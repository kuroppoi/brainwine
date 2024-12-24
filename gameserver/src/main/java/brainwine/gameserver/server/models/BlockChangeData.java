package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;

@JsonFormat(shape = Shape.ARRAY)
public class BlockChangeData {
    
    private final int x;
    private final int y;
    private final Layer layer;
    private final int entityId;
    private final Item item;
    private final int mod;
    
    public BlockChangeData(int x, int y, Layer layer, int entityId, Item item, int mod) {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.entityId = entityId;
        this.item = item;
        this.mod = mod;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Layer getLayer() {
        return layer;
    }
    
    public int getEntityId() {
        return entityId;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getMod() {
        return mod;
    }
}
