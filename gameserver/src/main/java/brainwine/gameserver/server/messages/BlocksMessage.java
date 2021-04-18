package brainwine.gameserver.server.messages;

import java.util.Collection;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.zone.Chunk;

public class BlocksMessage extends Message {
    
    public Collection<Chunk> chunks;
    
    public BlocksMessage(Collection<Chunk> chunks) {
        this.chunks = chunks;
    }
    
    @Override
    public boolean isCompressed() {
        return true;
    }
    
    @Override
    public boolean isPrepacked() {
        return true;
    }
}
