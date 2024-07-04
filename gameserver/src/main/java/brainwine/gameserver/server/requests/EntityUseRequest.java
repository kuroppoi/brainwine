package brainwine.gameserver.server.requests;

import java.util.List;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 46)
public class EntityUseRequest extends PlayerRequest {

    public int entityId;
    public Object data;
    
    @Override
    public void process(Player player) {
        // Do nothing if player is dead
        if(player.isDead()) {
            return;
        }
        
        Entity entity = player.getZone().getEntity(entityId);
        
        // Check if entity exists
        if(entity == null) {
            return;
        }
        
        // Check if entity is player
        if(entity.isPlayer()) {
            Player targetPlayer = (Player)entity;
            
            if(data instanceof List<?>) {
                List<Object> data = (List<Object>)this.data;
                
                // Handle trade
                if(data.size() == 2 && "trade".equals(data.get(0)) && data.get(1) instanceof Integer) {
                    Item item = ItemRegistry.getItem((int)data.get(1));
                    player.tradeItem(targetPlayer, item);
                }
            }
            
            return;
        }
                
        // Handle NPC interaction
        Npc npc = (Npc)entity;
        
        if(data instanceof List<?>) {
            npc.interact(player, ((List<?>)data).toArray());
        } else {
            npc.interact(player, data);
        }
    }
}
