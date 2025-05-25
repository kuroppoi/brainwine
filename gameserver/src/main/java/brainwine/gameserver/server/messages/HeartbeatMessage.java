package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 143)
public class HeartbeatMessage extends Message {
    
    public int time;
    
    public HeartbeatMessage(int time) {
        this.time = time;
    }
}
