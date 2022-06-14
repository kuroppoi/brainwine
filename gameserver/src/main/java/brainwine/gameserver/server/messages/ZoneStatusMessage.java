package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 17)
public class ZoneStatusMessage extends Message {
    
    public Map<String, Object> status;
    
    public ZoneStatusMessage(Map<String, Object> status) {
        this.status = status;
    }
}
