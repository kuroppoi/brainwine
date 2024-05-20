package brainwine.gameserver.server.requests;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 54)
public class StatusRequest extends PlayerRequest {
    
    public Object status;
    
    @Override
    public void process(Player player) {
        
    }
}
