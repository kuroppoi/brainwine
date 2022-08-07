package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 61)
public class LevelMessage extends Message {
    
    public int level;
    
    public LevelMessage(int level) {
        this.level = level;
    }
}
