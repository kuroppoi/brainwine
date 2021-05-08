package brainwine.gameserver.server.requests;

import org.msgpack.type.Value;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class EventRequest extends PlayerRequest {
    
    public String key;
    public Value value;
    
    @Override
    public void process(Player player) {}
}
