package brainwine.gameserver.server.requests;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 26)
public class RespawnRequest extends PlayerRequest {
    
    public Object status;
    
    @Override
    public void process(Player player) {
        player.respawn();
    }
}
