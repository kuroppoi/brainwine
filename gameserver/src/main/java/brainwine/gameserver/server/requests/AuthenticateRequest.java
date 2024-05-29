package brainwine.gameserver.server.requests;

import java.time.format.DateTimeFormatter;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.player.PlayerRestriction;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.Request;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 1)
public class AuthenticateRequest extends Request {
    
    public String version;
    public String name;
    public String authToken;
    
    @OptionalField
    public Object details;
    
    @Override
    public void process(Connection connection) {
        GameServer server = GameServer.getInstance();
        PlayerManager playerManager = server.getPlayerManager();
        
        if(!PlayerManager.SUPPORTED_VERSIONS.contains(version)) {
            connection.kick("Sorry, this version of Deepworld is not supported.");
            return;
        }
        
        connection.submitTask(() -> playerManager.verifyAuthToken(name, authToken)).addListener(future -> {
            // TODO can this err?
            if(!(boolean)future.get()) {
                connection.kick("The provided session token is invalid or has expired. Please try relogging.");
                return;
            }
            
            server.queueSynchronousTask(() -> {
                Player player = playerManager.getPlayer(name);
                PlayerRestriction ban = player.getCurrentBan();
                Zone zone = player.getZone();
                
                // Deny access if the player is currently banned
                if(ban != null) {
                    connection.kick(String.format("You are banned from the server until %s for: %s", 
                            ban.getEndDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), ban.getReason()));
                    return;
                }
                
                // Try to put player in a random zone if current zone is null
                if(zone == null) {
                    zone = server.getZoneManager().findBeginnerZone();
                }
                
                // Kick player if zone is still null (aka it failed to find a suitable random zone)
                if(zone == null) {
                    connection.kick("No default zone could be found.");
                    return;
                }
                
                player.setConnection(connection);
                player.setClientVersion(version);
                zone.addEntity(player);
                playerManager.onPlayerConnect(player);
            });
        });
    }
}
