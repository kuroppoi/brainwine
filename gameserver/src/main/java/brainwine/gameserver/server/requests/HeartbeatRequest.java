package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 143)
public class HeartbeatRequest extends PlayerRequest {
    
    public int latency;
    public int requestLatency;
    
    @Override
    public void process(Player player) {
        player.heartbeat();
    }
}
