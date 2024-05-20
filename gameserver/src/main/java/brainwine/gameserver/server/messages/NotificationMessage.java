package brainwine.gameserver.server.messages;

import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 33)
public class NotificationMessage extends Message {
    
    public Object message;
    public NotificationType type;
    
    public NotificationMessage(Object message, NotificationType type) {
        this.message = message;
        this.type = type;
    }
}
