package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 8, collection = true)
public class EntityChangeMessage extends Message {
    
    public int id;
    public Map<String, Object> details;
    
    public EntityChangeMessage(int id, Map<String, Object> details) {
        this.id = id;
        this.details = details;
    }
}
