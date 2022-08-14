package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.models.BlockChangeData;

@MessageInfo(id = 9, prepacked = true)
public class BlockChangeMessage extends Message {

    public Collection<BlockChangeData> blockChanges;
    
    public BlockChangeMessage(Collection<BlockChangeData> blockChanges) {
        this.blockChanges = blockChanges;
    }
    
    public BlockChangeMessage(int x, int y, Layer layer, Item item, int mod) {
        this(Arrays.asList(new BlockChangeData(x, y, layer, item, mod)));
    }
}
