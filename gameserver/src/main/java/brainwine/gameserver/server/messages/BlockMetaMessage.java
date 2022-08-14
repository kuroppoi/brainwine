package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.zone.MetaBlock;

@MessageInfo(id = 20, prepacked = true)
public class BlockMetaMessage extends Message {
    
    public Collection<MetaBlock> metaBlocks;
    
    public BlockMetaMessage(Collection<MetaBlock> metaBlocks) {
        this.metaBlocks = metaBlocks;
    }
    
    public BlockMetaMessage(MetaBlock metaBlock) {
        this(Arrays.asList(metaBlock));
    }
    
    // TODO Kind of evil...
    public BlockMetaMessage(int x, int y) {
        this(new MetaBlock(x, y));
    }
}
