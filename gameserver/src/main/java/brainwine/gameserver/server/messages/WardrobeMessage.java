package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class WardrobeMessage extends Message {
    
    public int[] ids;
    
    public WardrobeMessage(int[] ids) {
        this.ids = ids;
    }
}
