package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 51)
public class EntitiesRequest extends PlayerRequest {
    
    public int[] entityIds;
    
    public void process(Player player) {
        // TODO
    }
}
