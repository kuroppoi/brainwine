package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class EventRequest extends PlayerRequest {
    
    public String key;
    public Object value;
    
    @Override
    public void process(Player player) {}
}
