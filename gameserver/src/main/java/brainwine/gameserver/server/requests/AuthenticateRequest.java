package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.PlayerManager;
import brainwine.gameserver.server.Request;
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
                player.setConnection(connection);
                player.setClientVersion(version);
                Zone zone = player.getZone();
                
                if(zone == null) {
                    // TODO default zone 'n stuff.
                    zone = server.getZoneManager().getRandomZone();
                }
                
                if(zone == null) {
                    player.kick("No default zone could be found.");
                    return;
                }
                
                playerManager.onPlayerConnect(player);
                zone.addEntity(player);
            });
        });
    }
}
