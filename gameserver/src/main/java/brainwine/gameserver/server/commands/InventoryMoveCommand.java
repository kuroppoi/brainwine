package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.player.ContainerType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 14)
public class InventoryMoveCommand extends PlayerCommand {
    
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
