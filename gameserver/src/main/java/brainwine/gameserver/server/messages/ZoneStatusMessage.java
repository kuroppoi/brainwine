package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 17)
public class ZoneStatusMessage extends Message {
    
    public Object status;
    
    public ZoneStatusMessage(Object status) {
        this.status = status;
    }
}
