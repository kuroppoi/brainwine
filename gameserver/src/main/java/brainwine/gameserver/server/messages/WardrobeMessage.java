package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 39)
public class WardrobeMessage extends Message {
    
    public int[] ids;
    
    public WardrobeMessage(int[] ids) {
        this.ids = ids;
    }
}
