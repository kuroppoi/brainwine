package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;

public class MetaBlock {
    
    private final int x;
    private final int y;
    private final Item item;
    private String owner;
    private Map<String, Object> metadata;
    
    public MetaBlock(int x, int y) {
        this(x, y, Item.AIR);
    }
    
    public MetaBlock(int x, int y, Item item) {
        this(x, y, item, new HashMap<String, Object>());
    }
    
    public MetaBlock(int x, int y, Item item, Map<String, Object> metadata) {
        this.x = x;
        this.y = y;
        this.item = item;
        this.metadata = metadata;
        setMetadata(metadata);
    }
    
    @ConstructorProperties({"x", "y", "item", "owner", "metadata"})
    private MetaBlock(int x, int y, int item, String owner, Map<String, Object> metadata) {
        this(x, y, ItemRegistry.getItem(item), metadata);
        this.owner = owner;
    }
    
    @JsonValue
    public Map<String, Object> getJsonValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x", x);
        map.put("y", y);
        map.put("item", item.getId());
        map.put("metadata", metadata);
        
        // Don't include owner if there isn't one.
        if(hasOwner()) {
            map.put("owner", owner);
        }
        
        return map;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public boolean hasOwner() {
        return owner != null;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Item getItem() {
        return item;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
