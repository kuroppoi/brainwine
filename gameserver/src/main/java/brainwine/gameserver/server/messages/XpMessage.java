package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

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
