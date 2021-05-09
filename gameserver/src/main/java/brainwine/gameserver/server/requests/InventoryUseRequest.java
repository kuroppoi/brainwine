package brainwine.gameserver.server.requests;

import org.msgpack.type.Value;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.EntityItemUseMessage;

/**
 * TODO This request may be sent *before* a {@link CraftRequest} is sent.
 * So basically, we can't really perform any "has item" checks...
 */
public class InventoryUseRequest extends PlayerRequest {
    
    public int type; // 0 = main, 1 = secondary
    public Item item;
    public int status; // 0 = select, 1 = start, 2 = stop
    
    @OptionalField
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
