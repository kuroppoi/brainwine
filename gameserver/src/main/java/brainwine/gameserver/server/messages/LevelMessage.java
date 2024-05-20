package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 61)
public class LevelMessage extends Message {
    
    public int level;
    
    public LevelMessage(int level) {
        this.level = level;
    }
}
