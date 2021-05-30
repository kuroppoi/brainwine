package brainwine.gameserver.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    public static final Item AIR = new Item() {
        
        @Override
        public int getId() {
            return 0;
        }
        
        @Override
        public String getName() {
            return "air";
        }
    };
    
    @JsonProperty("code")
    private int id;
    
    @JacksonInject("name") 
    private String name;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("loot_graphic")
    private LootGraphic lootGraphic = LootGraphic.NONE;
    
    @JsonProperty("action")
    private Action action = Action.NONE;
    
    @JsonProperty("layer")
    private Layer layer = Layer.NONE;
    
    @JsonProperty("mod")
    private ModType mod = ModType.NONE;
    
    @JsonProperty("meta")
    private MetaType meta = MetaType.NONE;
    
    @JsonProperty("field")
    private int field;
    
    @JsonProperty("diggable")
    private boolean diggable;
    
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
    private LazyItemGetter inventoryItem;
    
    @JsonProperty("decay inventory")
    private LazyItemGetter decayInventoryItem;
    
    @JsonProperty("crafting quantity")
    private int craftingQuantity = 1;
    
    @JsonProperty("loot")
    private String[] lootCategories = {};
    
    @JsonProperty("ingredients")
    private List<CraftingIngredient> ingredients = new ArrayList<>();
    
    @JsonProperty("use")
    private Map<ItemUseType, Object> useConfigs = new HashMap<>();
    
    private Item(){}
    
    @JsonCreator
    private static Item fromId(int id) {
        return ItemRegistry.getItem(id);
    }
    
    @JsonCreator
    private static Item fromName(String name) {
        return ItemRegistry.getItem(name);
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
    
    public int getId() {
        return id;
    }
    
    @JsonValue
    public String getName() {
        return name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public boolean isAir() {
        return id == 0;
    }
    
    public LootGraphic getLootGraphic() {
        return lootGraphic;
    }
    
    public Action getAction() {
        return action;
    }
    
    public boolean isPlacable() {
        return layer == Layer.BACK || layer == Layer.FRONT;
    }
    
    public Layer getLayer() {
        return layer;
    }
    
    public boolean hasMod() {
        return mod != ModType.NONE;
    }
    
    public ModType getMod() {
        return mod;
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
    
    public boolean isDiggable() {
        return diggable;
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
        return inventoryItem == null ? this : inventoryItem.get();
    }
    
    public Item getDecayInventoryItem() {
        return decayInventoryItem == null ? this : decayInventoryItem.get();
    }
    
    public String[] getLootCategories() {
        return lootCategories;
    }
    
    public int getCraftingQuantity() {
        return craftingQuantity;
    }
    
    public boolean isCraftable() {
        return !ingredients.isEmpty();
    }
    
    public List<CraftingIngredient> getIngredients() {
        return ingredients;
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
