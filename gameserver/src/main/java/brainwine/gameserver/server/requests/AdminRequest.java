package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 254)
public class AdminRequest extends PlayerRequest {
    
    public String key;
    public Object data;
    
    @Override
    public void process(Player player) {
        if(!player.isAdmin()) {
            return;
        }
        
        switch(key) {
            case "god": player.setGodMode(data == null || data.equals(1));
            default: break;
        }
    }
}
