package brainwine.gameserver.server.commands;

import org.msgpack.type.Value;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;
import brainwine.gameserver.server.messages.EntityItemUseMessage;

@RegisterCommand(id = 10)
public class InventoryUseCommand extends PlayerCommand {
    
    public int type; // 0 = main, 1 = secondary
    public Item item;
    public int status; // 0 = select, 1 = start, 2 = stop
    public Value details; // array
    
    @Override
    public void process(Player player) {
        if(type == 0) {
            if(status != 2) {
                player.setHeldItem(item);
            }
        }
        
        player.sendMessageToPeers(new EntityItemUseMessage(player.getId(), type, item, status));
    }
}
