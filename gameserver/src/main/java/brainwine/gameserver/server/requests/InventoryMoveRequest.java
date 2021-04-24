package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.ContainerType;
import brainwine.gameserver.entity.player.Inventory;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.PlayerRequest;

public class InventoryMoveRequest extends PlayerRequest {
    
    public Item item;
    public ContainerType container;
    public int slot;
    
    @Override
    public void process(Player player) {
        if(container == null) {
            return;
        }
        
        Inventory inventory = player.getInventory();
        
        if(!inventory.hasItem(item)) {
            player.alert("Sorry, you do not have that item.");
            return;
        }
        
        inventory.moveItemToContainer(item, container, slot);
    }
}
