package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 50)
public class TeleportMessage extends Message {
    
    public int x;
    public int y;
    
    public TeleportMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
