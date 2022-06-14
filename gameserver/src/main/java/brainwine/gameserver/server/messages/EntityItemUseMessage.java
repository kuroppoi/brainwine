package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 10, collection = true)
public class EntityItemUseMessage extends Message {
    
    public int entityId;
    public int type;
    public Item item;
    public int status;
    
    public EntityItemUseMessage(int entityId, int type, Item item, int status) {
        this.entityId = entityId;
        this.type = type;
        this.item = item;
        this.status = status;
    }
}
