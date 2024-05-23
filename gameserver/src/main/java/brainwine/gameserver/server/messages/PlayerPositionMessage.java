package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 5)
public class PlayerPositionMessage extends Message {
    
    public float x;
    public float y;
    
    public PlayerPositionMessage(float x, float y) {
        this.x = x;
        this.y = y;
    }
}