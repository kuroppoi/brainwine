package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;

public class BlockMetaMessage extends Message {
    
    public int x;
    public int y;
    public Map<String, Object> metadata;
    
    public BlockMetaMessage(MetaBlock block) {
        this.x = block.getX();
        this.y = block.getY();
        this.metadata = MapHelper.copy(block.getMetadata());
        this.metadata.put("i", block.getItem().getId());
        
        if(block.hasOwner()) {
            this.metadata.put("p", block.getOwner());
        }
    }
    
    public BlockMetaMessage(int x, int y, Map<String, Object> metadata) {
        this.x = x;
        this.y = y;
        this.metadata = metadata;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
