package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 57)
public class EventMessage extends Message {
    
    public String name;
    public Object parameter;
    
    public EventMessage(String name, Object parameter) {
        this.name = name;
        this.parameter = parameter;
    }
}
