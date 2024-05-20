package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 57)
public class EventRequest extends PlayerRequest {
    
    public String key;
    public Object value;
    
    @Override
    public void process(Player player) {}
}
