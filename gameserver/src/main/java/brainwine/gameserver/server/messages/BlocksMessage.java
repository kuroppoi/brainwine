package brainwine.gameserver.server.messages;

import java.util.Collection;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;
import brainwine.gameserver.zone.Chunk;

@RegisterMessage(id = 3, compressed = true, prepacked = true)
public class BlocksMessage extends Message {
    
    public Collection<Chunk> chunks;
    
    public BlocksMessage(Collection<Chunk> chunks) {
        this.chunks = chunks;
    }
}
