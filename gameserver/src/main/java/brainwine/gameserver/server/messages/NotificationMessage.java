package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 33)
public class NotificationMessage extends Message {
    
    public String text;
    public int type;
    
    public NotificationMessage(String text, int type) {
        this.text = text;
        this.type = type;
    }
}
