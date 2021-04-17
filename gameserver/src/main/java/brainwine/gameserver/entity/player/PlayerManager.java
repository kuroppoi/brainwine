package brainwine.gameserver.entity.player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.zone.Zone;

public class PlayerManager {
    
    public static final String GAME_VERSION = "3.13.1";
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, Player> playersById = new HashMap<>();
    private final Map<String, Player> playersByName = new HashMap<>();
    private final Map<Connection, Player> playersByConnection = new HashMap<>();
    
    public PlayerManager() {
        loadPlayers();
    }
    
    public void tick() {
        
    }
    
    private void loadPlayers() {
        logger.info("Loading player data ...");
        File dataDir = new File("players");
        dataDir.mkdirs();
        
        for(File file : dataDir.listFiles()) {
            if(!file.isDirectory()) {
                loadPlayer(file);
            }
        }
        
        logger.info("Successfully loaded {} player(s)", playersById.size());
    }
    
    private void loadPlayer(File file) {
        String id = file.getName().replace(".json", "");
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectableValues = new InjectableValues.Std();
        injectableValues.addValue("documentId", id);
        mapper.setInjectableValues(injectableValues);
        
        try {
            Player player = mapper.readValue(file, Player.class);
            String name = player.getName();
            
            if(playersByName.containsKey(name)) {
                logger.warn("Duplicate name {} for player id {}", name, id);
                return;
            }
            
            playersById.put(id, player);
            playersByName.put(name.toLowerCase(), player);
        } catch (Exception e) {
            logger.error("Could not load configuration for player id {}", id, e);
        }
    }
    
    public void savePlayers() {
        for(Player player : playersById.values()) {
            savePlayer(player);
        }
    }
    
    public void savePlayer(Player player) {
        File file = new File("players", player.getDocumentId() + ".json");
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, player);
        } catch(Exception e) {
            logger.error("Could not save player id {}", player.getDocumentId(), e);
        }
    }
    
    public String refreshAuthToken(String name) {
        Player player = getPlayer(name);
        String token = UUID.randomUUID().toString();
        String hash = BCrypt.hashpw(token, BCrypt.gensalt());
        player.setAuthToken(hash);
        return token;
    }
    
    public boolean verifyPassword(String name, String password) {
        Player player = getPlayer(name);
        return player == null ? false : BCrypt.checkpw(password, player.getPassword());
    }
    
    public boolean verifyAuthToken(String name, String token) {
        Player player = getPlayer(name);
        return player == null ? false : BCrypt.checkpw(token, player.getAuthToken());
    }
    
    public void registerPlayer(String name) {
        if(getPlayer(name) != null) {
            return;
        }
        
        String id = UUID.randomUUID().toString();
        Player player = new Player(id, name, GameServer.getInstance().getZoneManager().getRandomZone()); // TODO tutorial zone
        player.setPassword(BCrypt.hashpw("password", BCrypt.gensalt())); // For now, too lazy to do registration.
        playersById.put(id, player);
        playersByName.put(name.toLowerCase(), player);
    }
    
    public void onPlayerAuthenticate(Connection connection, String version, String name, String authToken) {
        if(!version.equals(GAME_VERSION)) {
            connection.kick("Outdated version. Please update your game.", false);
            return;
        }
        
        Player player = getPlayer(name);
        
        if(!verifyAuthToken(name, authToken)) {
            connection.kick("The session token is either invalid or has expired.");
            return;
        }
        
        player.setConnection(connection);
        playersByConnection.put(connection, player);
        Zone zone = player.getZone();
        
        if(zone == null) {
            // TODO default zone 'n stuff.
            zone = GameServer.getInstance().getZoneManager().getRandomZone();
        }
        
        if(zone == null) {
            player.kick("No default zone could be found.");
            return;
        }
        
        zone.addPlayer(player);
    }
    
    public Player getPlayer(String name) {
        return playersByName.get(name.toLowerCase());
    }
        
    public Player getPlayer(Connection connection) {
        return playersByConnection.get(connection);
    }
}
