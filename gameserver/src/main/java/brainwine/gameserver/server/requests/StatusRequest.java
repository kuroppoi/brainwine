package brainwine.gameserver.server.requests;

import org.msgpack.type.Value;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class StatusRequest extends PlayerRequest {
    
    public Value status;
    
    @Override
    public void process(Player player) {
        
    }
}
