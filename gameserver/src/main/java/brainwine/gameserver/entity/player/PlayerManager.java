package brainwine.gameserver.entity.player;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.shared.JsonHelper;

public class PlayerManager {
    
    // TODO check platforms as well
    public static final List<String> SUPPORTED_VERSIONS = Arrays.asList("2.11.0.1", "2.11.1", "3.13.1");
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
        logger.info(SERVER_MARKER, "Loading player data ...");
        File dataDir = new File("players");
        dataDir.mkdirs();
        
        for(File file : dataDir.listFiles()) {
            if(!file.isDirectory()) {
                loadPlayer(file);
            }
        }
        
        logger.info(SERVER_MARKER, "Successfully loaded {} player(s)", playersById.size());
    }
    
    private void loadPlayer(File file) {
        String id = file.getName().replace(".json", "");
        
        try {
            PlayerConfigFile configFile = JsonHelper.readValue(file, PlayerConfigFile.class);
            Player player = new Player(id, configFile);
            
            if(player.getZone() == null) {
                player.setZone(GameServer.getInstance().getZoneManager().getRandomZone());
            }
            
            String name = player.getName();
            
            if(playersByName.containsKey(name)) {
                logger.warn(SERVER_MARKER, "Duplicate name {} for player id {}", name, id);
                return;
            }
            
            playersById.put(id, player);
            playersByName.put(name.toLowerCase(), player);
        } catch (Exception e) {
            logger.error(SERVER_MARKER, "Could not load configuration for player id {}", id, e);
        }
    }
    
    public void savePlayers() {
        for(Player player : playersById.values()) {
            savePlayer(player);
        }
    }
    
    public void savePlayer(Player player) {
        File file = new File("players", player.getDocumentId() + ".json");
        
        try {
            JsonHelper.writeValue(file, new PlayerConfigFile(player));
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not save player id {}", player.getDocumentId(), e);
        }
    }
    
    public String register(String name) {
        if(getPlayer(name) != null) {
            return null;
        }
        
        String id = UUID.randomUUID().toString();
        Player player = new Player(id, name, GameServer.getInstance().getZoneManager().getRandomZone()); // TODO tutorial zone
        playersById.put(id, player);
        playersByName.put(name.toLowerCase(), player);
        String authToken = UUID.randomUUID().toString();
        player.addAuthToken(BCrypt.hashpw(authToken, BCrypt.gensalt()));
        return authToken;
    }
    
    public String login(String name, String password) {
        Player player = getPlayer(name);
        
        if(player == null || player.getPassword() == null) {
            return null;
        }
        
        if(!BCrypt.checkpw(password, player.getPassword())) {
           return null; 
        }
        
        String authToken = UUID.randomUUID().toString();
        player.addAuthToken(BCrypt.hashpw(authToken, BCrypt.gensalt()));
        return authToken;
    }
    
    public boolean verifyAuthToken(String name, String authToken) {
        Player player = getPlayer(name);
        
        if(player == null) {
            return false;
        }
        
        player.clearOldestAuthTokens();
        
        // Might not be very efficient...
        for(String hashedToken : player.getAuthTokens()) {
            if(BCrypt.checkpw(authToken, hashedToken)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void onPlayerConnect(Player player) {
        Connection connection = player.getConnection();
        playersByConnection.put(connection, player);
        logger.info(SERVER_MARKER, "{} logged into zone {} from {}", player.getName(), player.getZone().getName(), connection.getAddress());
    }
    
    public void onPlayerDisconnect(Player player) {
        Connection connection = player.getConnection();
        playersByConnection.remove(connection);
        logger.info(SERVER_MARKER, "{} disconnected: {}", player.getName(), connection.getDisconnectReason());
    }
    
    public boolean isEmailTaken(String email) {
        for(Player player : getPlayers()) {
            if(email.equalsIgnoreCase(player.getEmail())) {
                return true;
            }
        }
        
        return false;
    }
    
    public Player getPlayer(String name) {
        return playersByName.get(name.toLowerCase());
    }
    
    public Player getPlayerById(String id) {
        return playersById.get(id);
    }
    
    public Player getPlayer(Connection connection) {
        return playersByConnection.get(connection);
    }
    
    public Collection<Player> getPlayers() {
        return playersById.values();
    }
}
