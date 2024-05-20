package brainwine.gameserver.server.requests;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
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
        // Don't do anything if the player is dead or doesn't own this item
        if(player.isDead() || (!item.isAir() && !player.getInventory().hasItem(item))) {
            return;
        }
        
        // Try to consume item if it is a consumable
        if(item.isConsumable()) {
            if(status == 1) {
                player.consume(item, details);
            }
        } else {
            // Set current held item if applicable
            if(type == 0 && status != 2) {
                player.setHeldItem(item);
            }
            
            // Send item use data to other players in the zone
            player.sendMessageToTrackers(new EntityItemUseMessage(player.getId(), type, item, status));
            
            // Lovely type ambiguity. Always nice.
            if(item.isWeapon() && status == 1) {
                Collection<?> entityIds = details instanceof Collection ? (Collection<?>)details
                        : details instanceof Integer ? Arrays.asList((int)details) : null;
                
                // Skip if null aka details was of an invalid type
                if(entityIds == null) {
                    return;
                }
                
                int maxTargetableEntities = player.getMaxTargetableEntities();
                
                for(Object id : entityIds) {
                    if(id instanceof Integer) {
                        Npc npc = player.getZone().getNpc((int)id);
                        
                        if(npc != null && (player.isGodMode() || (player.canSee(npc) && !npc.wasAttackedRecently(player, Entity.ATTACK_INVINCIBLE_TIME)))) {
                            npc.attack(player, item, item.getDamage(), item.getDamageType());
                        }
                    }
                    
                    if(--maxTargetableEntities <= 0) {
                        break;
                    }
                }
            }
        }
    }
}
