package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;

public class EntityChangeMessage extends Message {
    
    public int id;
    public Map<String, Object> details;
    
    public EntityChangeMessage(int id, Map<String, Object> details) {
        this.id = id;
        this.details = details;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
