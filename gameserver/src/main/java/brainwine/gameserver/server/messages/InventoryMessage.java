package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.entity.player.Inventory;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 4)
public class InventoryMessage extends Message {
    
    public Map<String, Object> data;
    
    public InventoryMessage(Inventory inventory) {
        this(inventory.getClientConfig());
    }
    
    public InventoryMessage(Map<String, Object> data) {
        this.data = data;
    }
}
