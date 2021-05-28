package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class StatMessage extends Message {
    
    public String key;
    public Object value;
    
    public StatMessage(String key, Object value) {
        this.key = key;
        this.value = value;
    }
    
    public boolean isCollection() {
        return true;
    }
}
