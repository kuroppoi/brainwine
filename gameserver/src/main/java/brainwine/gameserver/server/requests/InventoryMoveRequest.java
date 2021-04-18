package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.ContainerType;
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
        
        player.getInventory().moveItemToContainer(item, container, slot);
    }
}
