package brainwine.gameserver.server.requests;

import java.util.List;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 46)
public class EntityUseRequest extends PlayerRequest {

    public int entityId;
    public Object data;
    
    @Override
    public void process(Player player) {
        Entity entity = player.getZone().getEntity(entityId);
        
        // Check if entity exists
        if(entity == null) {
            return;
        }
        
        // Check if entity is player
        if(entity.isPlayer()) {
            Player targetPlayer = (Player)entity;
            
            if(data instanceof List<?>) {
                List<?> data = (List<?>)this.data;
                
                // Handle trade
                if(data.size() == 2 && "trade".equals(data.get(0)) && data.get(1) instanceof Integer) {
                    Item item = ItemRegistry.getItem((int)data.get(1));
                    player.tradeItem(targetPlayer, item);
                }
            }
            
            return;
        }
    }
}
