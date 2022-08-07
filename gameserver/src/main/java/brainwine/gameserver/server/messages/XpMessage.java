package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 60)
public class XpMessage extends Message {
    
    public int change;
    public int total;
    public String message;
    
    public XpMessage(int change, int total) {
        this(change, total, null);
    }
    
    public XpMessage(int change, int total, String message) {
        this.change = change;
        this.total = total;
        this.message = message;
    }
}
