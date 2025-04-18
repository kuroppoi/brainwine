package brainwine.gameserver.player;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.util.MapHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import brainwine.gameserver.server.pipeline.Connection;
import brainwine.shared.JsonHelper;
import brainwine.shared.TokenGenerator;

public class PlayerManager {
    public static Map<String, String> SUPPORTED_VERSIONS = MapHelper.map(
            String.class, String.class,
            "iPh|iPo|iPa", "2.11.0",
            "Windows", "3.11.0",
            "Unity", "3.11.0",
            "default", "2.11.0"
    );
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, Player> playersById = new HashMap<>();
    private final Map<String, Player> playersByName = new HashMap<>();
    private final Map<String, Player> apiTokens = new HashMap<>();
    private final List<Player> onlinePlayers = new ArrayList<>();
    
    public PlayerManager() {
        loadSupportedVersions();
        loadPlayers();
    }

    private void loadSupportedVersions() {
        Map<String, String> cfg = MapHelper.getMap(GameConfiguration.getBaseConfig(), "client_version");
        if(cfg != null) {
            SUPPORTED_VERSIONS = cfg;
        } else {
            logger.warn(SERVER_MARKER, "Supported client versions are not configured.");
        }
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
            String name = player.getName();
            
            if(playersByName.containsKey(name)) {
                logger.warn(SERVER_MARKER, "Duplicate name {} for player id {}", name, id);
                return;
            }
            
            playersById.put(id, player);
            playersByName.put(name.toLowerCase(), player);
            
            if(player.getApiToken() != null) {
                apiTokens.put(player.getApiToken(), player);
            }
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
        Player player = new Player(id, name, null); // TODO tutorial zone
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
    
    public boolean issueApiToken(Player player) {
        String apiToken = TokenGenerator.generateToken(10, apiTokens::containsKey);
        String currentToken = player.getApiToken();
        
        if(apiToken == null) {
            player.notify("Oops, we couldn't issue an API token for you.", NotificationType.SYSTEM);
            return false;
        }
        
        if(currentToken != null && !apiTokens.remove(currentToken, player)) {
            logger.warn(SERVER_MARKER, "Could not unindex API token {} for player {}", currentToken, player.getDocumentId());
        }
        
        player.setApiToken(apiToken);
        apiTokens.put(apiToken, player);
        return true;
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
    
    public void changePlayerName(Player player, String name) {
        if(playersByName.containsKey(name)) {
            logger.warn(SERVER_MARKER, "Tried to rename player {} to already existing name {}", player.getDocumentId(), name);
            return;
        }
        
        // Track name change and re-index the player
        playersByName.remove(player.getName().toLowerCase());
        player.trackNameChange(name);
        player.setName(name);
        playersByName.put(name.toLowerCase(), player);
    }
    
    public void onPlayerConnect(Player player) {
        onlinePlayers.add(player);
        logger.info(SERVER_MARKER, "{} logged into zone {}", player.getName(), player.getZone().getName());
    }
    
    public void onPlayerDisconnect(Player player) {
        Connection connection = player.getConnection();
        onlinePlayers.remove(player);
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
    
    public Player getPlayerByApiToken(String apiToken) {
        return apiTokens.get(apiToken);
    }
    
    public Collection<Player> getPlayers() {
        return playersById.values();
    }
    
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }
    
    public List<Player> getOnlinePlayers() {
        return Collections.unmodifiableList(onlinePlayers);
    }
}
