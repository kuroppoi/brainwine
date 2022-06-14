package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 26)
public class RespawnRequest extends PlayerRequest {
    
    public Object status;
    
    @Override
    public void process(Player player) {
        player.respawn();
    }
}
