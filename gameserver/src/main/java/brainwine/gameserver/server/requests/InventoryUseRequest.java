package brainwine.gameserver.server.requests;

import java.util.Collection;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.EntityItemUseMessage;

/**
 * TODO This request may be sent *before* a {@link CraftRequest} is sent.
 * So basically, we can't really perform any "has item" checks...
 * ... Let's do it anyway lol!
 */
@RequestInfo(id = 10)
public class InventoryUseRequest extends PlayerRequest {
    
    public int type; // 0 = main, 1 = secondary
    public Item item;
    public int status; // 0 = select, 1 = start, 2 = stop
    
    @OptionalField
    public Object details; // array
    
    @Override
    public void process(Player player) {
        if(player.isDead() || !player.getInventory().hasItem(item)) {
            return;
        }
        
        if(type == 0) {
            if(status != 2) {
                player.setHeldItem(item);
            }
            
            // Use item
            if(status == 1) {
                if(item.isConsumable()) {
                    player.consume(item);
                }
                
                // Lovely type ambiguity. Always nice.
                if(item.isWeapon() && details instanceof Collection) {
                    Collection<?> entityIds = (Collection<?>)details;
                    int maxTargetableEntities = player.getMaxTargetableEntities();
                    
                    for(Object id : entityIds) {
                        if(id instanceof Integer) {
                            Npc npc = player.getZone().getNpc((int)id);
                            
                            if(npc != null && player.canSee(npc)) {
                                npc.attack(player, item);
                            }
                        }
                        
                        if(--maxTargetableEntities <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        
        player.sendMessageToPeers(new EntityItemUseMessage(player.getId(), type, item, status));
    }
}
