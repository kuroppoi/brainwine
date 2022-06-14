package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 255)
public class KickMessage extends Message {
    
    public String reason;
    public boolean shouldReconnect;
    
    public KickMessage(String reason, boolean shouldReconnect) {
        this.reason = reason;
        this.shouldReconnect = shouldReconnect;
    }
}
