package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class ZoneExploredMessage extends Message {
    
    public int chunkIndex;
    
    public ZoneExploredMessage(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
