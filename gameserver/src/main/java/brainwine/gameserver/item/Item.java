package brainwine.gameserver.item;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    public static final Item AIR = new Item();
    
    @JsonProperty("code")
    private int id;
    
    @JacksonInject("name") 
    private String name;
    
    @JsonProperty("layer")
    private Layer layer = Layer.NONE;
    
    @JsonProperty("meta")
    private MetaType meta = MetaType.NONE;
    
    @JsonProperty("field")
    private int field;
    
    @JsonProperty("wardrobe")
    private boolean clothing;
    
    @JsonProperty("placeover")
    private boolean placeover;
    
    @JsonProperty("base")
    private boolean base;
    
    @JsonProperty("whole")
    private boolean whole;
    
    @JsonProperty("invulnerable")
    private boolean invulnerable;
    
    @JsonProperty("inventory")
    private String inventoryItem;
    
    @JsonProperty("use")
    private Map<ItemUseType, Object> useConfigs = new HashMap<>();
    
    private Item(){}
    
    @JsonCreator
    private static Item fromId(int id) {
        return ItemRegistry.getItem(id);
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Item)) {
            return false;
        }
        
        Item item = (Item)object; 
        return item.getId() == id;
    }
    
    @JsonValue
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isAir() {
        return id == 0;
    }
    
    public boolean isPlacable() {
        return layer == Layer.BACK || layer == Layer.FRONT;
    }
    
    public Layer getLayer() {
        return layer;
    }
    
    public boolean hasMeta() {
        return meta != MetaType.NONE;
    }
    
    public MetaType getMeta() {
        return meta;
    }
    
    public boolean isDish() {
        return field > 1;
    }
    
    public boolean hasField() {
        return field > 0;
    }
    
    public int getField() {
        return field;
    }
    
    public boolean isClothing() {
        return clothing;
    }
    
    public boolean isBase() {
        return base;
    }
    
    public boolean canPlaceOver() {
        return placeover;
    }
    
    public boolean isWhole() {
        return whole;
    }
    
    public boolean isInvulnerable() {
        return invulnerable || !isPlacable();
    }
    
    public Item getInventoryItem() {
        if(inventoryItem == null) {
            return this;
        }
        
        return ItemRegistry.getItem(inventoryItem);
    }
    
    public boolean hasUse(ItemUseType... types) {
        for(ItemUseType type : types) {
            if(useConfigs.containsKey(type)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Map<ItemUseType, Object> getUses() {
        return useConfigs;
    }
}
