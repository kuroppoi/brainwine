package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 143)
public class HeartbeatMessage extends Message {
    
    public int time;
    
    public HeartbeatMessage(int time) {
        this.time = time;
    }
}
