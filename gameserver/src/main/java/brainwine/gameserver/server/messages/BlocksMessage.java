package brainwine.gameserver.server.messages;

import java.util.Collection;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.zone.Chunk;

@MessageInfo(id = 3, compressed = true, prepacked = true)
public class BlocksMessage extends Message {
    
    public Collection<Chunk> chunks;
    
    public BlocksMessage(Collection<Chunk> chunks) {
        this.chunks = chunks;
    }
}
