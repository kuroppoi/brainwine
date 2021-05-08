package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class HeartbeatMessage extends Message {
    
    public int time;
    
    public HeartbeatMessage(int time) {
        this.time = time;
    }
}
