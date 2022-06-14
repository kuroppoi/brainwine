package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 44, collection = true)
public class StatMessage extends Message {
    
    public String key;
    public Object value;
    
    public StatMessage(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}
