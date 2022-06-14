package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 53, collection = true)
public class ZoneExploredMessage extends Message {
    
    public int chunkIndex;
    
    public ZoneExploredMessage(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }
}
