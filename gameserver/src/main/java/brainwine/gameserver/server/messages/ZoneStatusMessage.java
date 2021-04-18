package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;

public class ZoneStatusMessage extends Message {
    
    public Map<String, Object> status;
    
    public ZoneStatusMessage(Map<String, Object> status) {
        this.status = status;
    }
}
