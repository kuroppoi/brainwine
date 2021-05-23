package brainwine.gameserver.server.messages;

import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.server.Message;

public class NotificationMessage extends Message {
    
    public Object message;
    public NotificationType type;
    
    public NotificationMessage(Object message, NotificationType type) {
        this.message = message;
        this.type = type;
    }
}
