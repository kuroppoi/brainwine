package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class StatusRequest extends PlayerRequest {
    
    public Object status;
    
    @Override
    public void process(Player player) {
        
    }
}
