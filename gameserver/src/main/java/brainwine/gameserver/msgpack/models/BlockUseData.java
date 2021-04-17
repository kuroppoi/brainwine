package brainwine.gameserver.msgpack.models;

import java.util.Map;

/**
 * For {@link BlockUseCommand}
 */
public class BlockUseData {
    
    private int[] position;
    private Map<String, Object> metadata;
    
    public BlockUseData(int[] position) {
        this.position = position;
    }
    
    public BlockUseData(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public boolean hasPosition() {
        return position != null && position.length == 2;
    }
    
    public int[] getPosition() {
        return position;
    }
    
    public boolean hasMetadata() {
        return metadata != null;
    }
    
    public Map<String, Object> getMetadata(){
        return metadata;
    }
}
