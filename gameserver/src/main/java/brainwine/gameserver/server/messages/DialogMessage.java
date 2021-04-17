package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 45, compressed = true)
public class DialogMessage extends Message {
    
    public int id;
    public Map<String, Object> config;
    
    public DialogMessage(int id, Map<String, Object> config) {
        this.id = id;
        this.config = config;
    }
}
