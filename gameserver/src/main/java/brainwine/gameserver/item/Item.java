package brainwine.gameserver.item;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    public static final Item AIR = new Item();
    private int id;
    
    @JacksonInject("name") 
    private String name;
    private Layer layer = Layer.NONE;
    private MetaType meta = MetaType.NONE;
    private int field;
    private boolean wardrobe;
    private boolean placeover;
    private boolean whole;
    private boolean invulnerable;
    private Map<ItemUseType, Object> useConfigs = new HashMap<>();
    
    @JsonProperty("code")
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
    
    public Layer getLayer()
    {
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
        return wardrobe;
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
    
    public boolean hasUse(ItemUseType... types) {
        for(ItemUseType type : types) {
            if(useConfigs.containsKey(type)) {
                return true;
            }
        }
        
        return false;
    }
    
    @JsonProperty("use")
    public Map<ItemUseType, Object> getUses() {
        return useConfigs;
    }
}
