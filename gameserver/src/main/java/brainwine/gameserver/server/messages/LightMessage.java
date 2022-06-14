package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 15, collection = true)
public class LightMessage extends Message {
    
    public int x;
    public int y; // Not used
    public int type; // Not used
    public int[] light;
    
    public LightMessage(int x, int[] light) {
        this.x = x;
        this.light = light;
    }
}
