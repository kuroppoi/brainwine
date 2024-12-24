package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 27)
public class FollowRequest extends PlayerRequest {
    
    public String recipientName;
    public boolean following;
    
    @Override
    public void process(Player player) {
        Player recipient = GameServer.getInstance().getPlayerManager().getPlayer(recipientName);
        
        // Check if recipient exists
        if(recipient == null) {
            player.notify(String.format("Couldn't find a player named %s", recipientName));
            return;
        }
        
        // Follow or unfollow player
        if(following) {
            player.followPlayer(recipient);
        } else {
            player.unfollowPlayer(recipient);
        }
    }
}
