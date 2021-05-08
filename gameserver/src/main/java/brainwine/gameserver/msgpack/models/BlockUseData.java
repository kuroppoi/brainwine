package brainwine.gameserver.msgpack.models;

/**
 * Simple wrapper to avoid having to force our custom template on object arrays.
 */
public class BlockUseData {
    
    private final Object[] data;
    
    public BlockUseData(Object[] data) {
        this.data = data;
    }
    
    public Object[] getData() {
        return data;
    }
}
