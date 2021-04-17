package brainwine.gameserver.server.messages;

import java.util.HashMap;
import java.util.Map;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;
import brainwine.gameserver.zone.MetaBlock;

@RegisterMessage(id = 20, collection = true)
public class BlockMetaMessage extends Message {
    
    public int x;
    public int y;
    public Map<String, Object> metadata;
    
    public BlockMetaMessage(int x, int y) {
        this(x, y, new HashMap<String, Object>());
    }
    
    public BlockMetaMessage(MetaBlock metaBlock) {
        this.x = metaBlock.getX();
        this.y = metaBlock.getY();
        
        // Create a separate map that includes the item id and owner of the MetaBlock.
        this.metadata = new HashMap<>();
        this.metadata.putAll(metaBlock.getMetadata());
        this.metadata.put("i", metaBlock.getItem().getId());
        
        if(metaBlock.hasOwner()) {
            this.metadata.put("p", metaBlock.getOwner());
        }
    }
    
    public BlockMetaMessage(int x, int y, Map<String, Object> metadata) {
        this.x = x;
        this.y = y;
        this.metadata = metadata;
    }
}
