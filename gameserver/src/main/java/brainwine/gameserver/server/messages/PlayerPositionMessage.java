package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 5)
public class PlayerPositionMessage extends Message {
    
    public int x;
    public int y;
    
    public PlayerPositionMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
