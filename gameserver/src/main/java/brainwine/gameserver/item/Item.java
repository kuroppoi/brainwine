package brainwine.gameserver.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.dialog.DialogType;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.util.Vector2i;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    public static final Item AIR = new Item("air", 0);
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("code")
    private int code;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("rotation")
    private String rotation;
    
    @JsonProperty("fieldable")
    private Fieldability fieldability = Fieldability.TRUE;
    
    @JsonProperty("loot_graphic")
    private DialogType lootGraphic = DialogType.STANDARD;
    
    @JsonProperty("action")
    private Action action = Action.NONE;
    
    @JsonProperty("layer")
    private Layer layer = Layer.NONE;
    
    @JsonProperty("mod")
    private ModType mod = ModType.NONE;
    
    @JsonProperty("meta")
    private MetaType meta = MetaType.NONE;
    
    @JsonProperty("group")
    private ItemGroup group = ItemGroup.NONE;
    
    @JsonProperty("size")
    private Vector2i size = new Vector2i(1, 1);
    
    @JsonProperty("field")
    private int field;
    
    @JsonProperty("xp")
    private int experienceYield;
    
    @JsonProperty("loot_xp")
    private int lootExperienceYield;
    
    @JsonProperty("guard")
    private int guardLevel;
    
    @JsonProperty("power")
    private float power;
    
    @JsonProperty("earthy")
    private boolean earthy;
    
    @JsonProperty("diggable")
    private boolean diggable;
    
    @JsonProperty("wardrobe")
    private boolean clothing;
    
    @JsonProperty("consumable")
    private boolean consumable;
    
    @JsonProperty("placeover")
    private boolean placeover;
    
    @JsonProperty("base")
    private boolean base;
    
    @JsonProperty("whole")
    private boolean whole;
    
    @JsonProperty("invulnerable")
    private boolean invulnerable;
    
    @JsonProperty("solid")
    private boolean solid;
    
    @JsonProperty("door")
    private boolean door;
    
    @JsonProperty("entity")
    private boolean entity;
    
    @JsonProperty("inventory")
    private LazyItemGetter inventoryItem;
    
    @JsonProperty("decay inventory")
    private LazyItemGetter decayInventoryItem;
    
    @JsonProperty("crafting quantity")
    private int craftingQuantity = 1;
    
    @JsonProperty("loot")
    private String[] lootCategories = {};
    
    @JsonProperty("skill_bonuses")
    private Map<Skill, Integer> skillBonuses = new HashMap<>();
    
    @JsonProperty("mining skill")
    private Pair<Skill, Integer> miningSkill;
    
    @JsonProperty("placing skill")
    private Pair<Skill, Integer> placingSkill;
    
    @JsonProperty("crafting skill")
    private Pair<Skill, Integer> craftingSkill;
    
    @JsonProperty("damage")
    private Pair<DamageType, Float> damageInfo;
    
    @JsonProperty("ingredients")
    private List<CraftingRequirement> craftingIngredients = new ArrayList<>();
    
    @JsonProperty("crafting_helpers")
    private List<CraftingRequirement> craftingHelpers = new ArrayList<>();
    
    @JsonProperty("use")
    private Map<ItemUseType, Object> useConfigs = new HashMap<>();
    
    @JsonCreator
    private Item(@JsonProperty(value = "id", required = true) String id,
            @JsonProperty(value = "code", required = true) int code) {
        this.id = id;
        this.code = code;
    }
    
    @JsonCreator
    public static Item get(String id) {
        return ItemRegistry.getItem(id);
    }
    
    @JsonCreator
    public static Item get(int code) {
        return ItemRegistry.getItem(code);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
    
    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Item)) {
            return false;
        }
        
        Item item = (Item)object; 
        return item.getId().equals(id);
    }
    
    @JsonValue
    @Override
    public String toString() {
        return id;
    }
    
    public boolean hasId(String id) {
        return this.id.equals(id);
    }
    
    public boolean hasCode(int code) {
        return this.code == code;
    }
    
    public String getId() {
        return id;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getTitle() {
        return title;
    }
    
    public boolean isMirrorable() {
        return rotation != null && rotation.equalsIgnoreCase("mirror");
    }
    
    public boolean isAir() {
        return code == 0;
    }
    
    public Fieldability getFieldability() {
        return fieldability;
    }
    
    public DialogType getLootGraphic() {
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
    
    public boolean hasGroup() {
        return group != ItemGroup.NONE;
    }
    
    public ItemGroup getGroup() {
        return group;
    }
    
    public int getBlockWidth() {
        return size.getX();
    }
    
    public int getBlockHeight() {
        return size.getY();
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
    
    public int getExperienceYield() {
        return experienceYield;
    }
    
    public int getLootExperienceYield() {
        return lootExperienceYield;
    }
    
    public int getGuardLevel() {
        return guardLevel;
    }
    
    public float getPower() {
        return power;
    }
    
    public boolean isEarthy() {
        return earthy;
    }
    
    public boolean isDiggable() {
        return diggable;
    }
    
    public boolean isClothing() {
        return clothing;
    }
    
    public boolean isConsumable() {
        return consumable;
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
    
    public boolean isDoor() {
        return door;
    }
    
    public boolean isSolid() {
        return solid;
    }
    
    public boolean isEntity() {
        return entity;
    }
    
    public Map<Skill, Integer> getSkillBonuses() {
        return skillBonuses;
    }
    
    public boolean requiresMiningSkill() {
        return miningSkill != null;
    }
    
    public Pair<Skill, Integer> getMiningSkill() {
        return miningSkill;
    }
    
    public boolean requiresPlacingSkill() {
        return placingSkill != null;
    }
    
    public Pair<Skill, Integer> getPlacingSkill() {
        return placingSkill;
    }
    
    public boolean requiresCraftingSkill() {
        return craftingSkill != null;
    }
    
    public Pair<Skill, Integer> getCraftingSkill() {
        return craftingSkill;
    }
    
    public boolean isWeapon() {
        return damageInfo != null;
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
    
    public DamageType getDamageType() {
        return isWeapon() ? damageInfo.getFirst() : DamageType.NONE;
    }
    
    public float getDamage() {
        return isWeapon() ? damageInfo.getLast() : 0;
    }
    
    public boolean isCraftable() {
        return !craftingIngredients.isEmpty();
    }
    
    public List<CraftingRequirement> getCraftingIngredients() {
        return craftingIngredients;
    }
    
    public boolean requiresWorkshop() {
        return !craftingHelpers.isEmpty();
    }
    
    public List<CraftingRequirement> getCraftingHelpers() {
        return craftingHelpers;
    }
    
    public boolean hasUse(ItemUseType... types) {
        for(ItemUseType type : types) {
            if(useConfigs.containsKey(type)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Object getUse(ItemUseType type) {
        return useConfigs.get(type);
    }
    
    public Map<ItemUseType, Object> getUses() {
        return useConfigs;
    }
}
