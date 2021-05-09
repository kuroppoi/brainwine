package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.ContainerType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;

/**
 * TODO This request may be sent *before* a {@link CraftRequest} is sent.
 * So basically, we can't really perform any "has item" checks...
 */
public class InventoryMoveRequest extends PlayerRequest {
    
    public Item item;
    
    @OptionalField
    public ContainerType container;
    
    @OptionalField
    public int slot;
    
    @Override
    public void process(Player player) {
        if(container == null) {
            return;
        }
        
        player.getInventory().moveItemToContainer(item, container, slot);
    }
}
