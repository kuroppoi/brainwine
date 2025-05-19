package brainwine.gameserver.server.requests;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.player.PlayerRestriction;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.Request;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.util.VersionUtils;
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

        String platform = "default";
        if(VersionUtils.isGreaterOrEqualTo(version, "3")) platform = "Unity";

        String wantedVersion = PlayerManager.SUPPORTED_VERSIONS.get(platform);
        if(wantedVersion == null) wantedVersion = PlayerManager.SUPPORTED_VERSIONS.get("default");
        if(wantedVersion == null) wantedVersion = "99";

        if(!VersionUtils.isGreaterOrEqualTo(version, wantedVersion)) {
            String defaultMessage = "Sorry, this version of Deepworld is not supported.";
            String message = PlayerManager.SUPPORTED_VERSIONS.get("message");
            connection.kick(message != null ? message : defaultMessage);
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
                
                if(ban != null) {
                    // Send player to jail world if they're banned
                    zone = server.getZoneManager().getZoneByName("Hell");
                    String banMessage = String.format("You are banned from the server until\n%s for: %s", 
                            ban.getEndDate().format(DateTimeFormatter.ofPattern("d MMMM uuuu HH:mm:ss", Locale.ENGLISH)), ban.getReason());
                    
                    // Kick player with ban message if no jail world exists
                    if(zone == null) {
                        connection.kick(banMessage);
                        return;
                    }
                    
                    connection.sendDelayedMessage(new NotificationMessage(banMessage, NotificationType.MAINTENANCE), 5000); // Slightly hacky but shouldn't cause any issues
                } else if(zone == null || !zone.canJoin(player)) {
                    // Try to put player in a random zone if current zone is null or cannot be joined
                    zone = player.getAchievements().isEmpty() ? server.getZoneManager().findTutorialZone() : server.getZoneManager().findBeginnerZone();
                    
                    // Kick player if zone is still null (aka it failed to find a suitable random zone)
                    if(zone == null) {
                        connection.kick("Sorry, we couldn't find a world for you to join.");
                        return;
                    }
                }
                
                player.setConnection(connection);
                player.setClientVersion(version);
                zone.addEntity(player);
                playerManager.onPlayerConnect(player);
            });
        });
    }
}
