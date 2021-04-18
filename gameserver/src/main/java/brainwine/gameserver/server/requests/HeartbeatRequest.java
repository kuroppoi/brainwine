package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class HeartbeatRequest extends PlayerRequest {
    
    public int latency;
    public int requestLatency;
    
    @Override
    public void process(Player player) {
        player.heartbeat();
    }
}
