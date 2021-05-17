package brainwine.gameserver.server.messages;

import brainwine.gameserver.command.NotificationType;
import brainwine.gameserver.server.Message;

public class NotificationMessage extends Message {
    
    public String text;
    public NotificationType type;
    
    public NotificationMessage(String text, NotificationType type) {
        this.text = text;
        this.type = type;
    }
}
