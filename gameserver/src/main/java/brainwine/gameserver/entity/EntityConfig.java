package brainwine.gameserver.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.Nulls;

import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityConfig {
    
    private final String name;
    private final int type;
    private int experienceYield;
    private float maxHealth = Entity.DEFAULT_HEALTH;
    private float baseSpeed = 3;
    private boolean character;
    private boolean human;
    private boolean named;
    private boolean trappable;
    private Item trappablePetItem;
    private Vector2i size = new Vector2i(1, 1);
    private EntityGroup group = EntityGroup.NONE;
    private WeightedMap<EntityLoot> loot = new WeightedMap<>();
    private WeightedMap<EntityLoot> placedLoot = new WeightedMap<>();
    private Map<Item, WeightedMap<EntityLoot>> lootByWeapon = new HashMap<>();
    private Map<DamageType, Float> resistances = new HashMap<>();
    private Map<DamageType, Float> weaknesses = new HashMap<>();
    private Map<String, String[]> components = new HashMap<>();
    private Map<String, String> attachments = new HashMap<>();
    private List<String> attachmentTypes = new ArrayList<>();
    private List<String> slots = new ArrayList<>();
    private List<String> animations = new ArrayList<>();
    private List<Map<String, Object>> behavior = new ArrayList<>();
    
    @JsonCreator
    private EntityConfig(@JacksonInject("name") String name,
            @JsonProperty(value = "code", required = true) int type) {
        this.name = name;
        this.type = type;
    }
    
    @JsonCreator
    public static EntityConfig fromName(String name) {
        return EntityRegistry.getEntityConfig(name);
    }
    
    @JsonValue
    public String getName() {
        return name;
    }
    
    public int getType() {
        return type;
    }

    @JsonIgnore
    public String getCategory() {
        String id = getName();
        
        int index = id.indexOf('/');
        return index > 1 ? id.substring(0, index) : null;
    }
    
    @JsonProperty("xp")
    public int getExperienceYield() {
        return experienceYield;
    }
    
    @JsonProperty("health")
    public float getMaxHealth() {
        return maxHealth;
    }
    
    @JsonProperty("speed")
    public float getBaseSpeed() {
        return baseSpeed;
    }
    
    public boolean isCharacter() {
        return character;
    }
    
    public boolean isHuman() {
        return human;
    }
    
    public boolean isNamed() {
        return named;
    }
    
    public boolean isTrappable() {
        return trappable;
    }
    
    public boolean hasTrappablePetItem() {
        return trappablePetItem != null && !trappablePetItem.isAir();
    }
    
    public Item getTrappablePetItem() {
        return trappablePetItem;
    }
    
    @JsonSetter(nulls = Nulls.SKIP)
    private void setSize(Vector2i size) {
        this.size = size;
    }
    
    public Vector2i getSize() {
        return size;
    }
    
    @JsonSetter(nulls = Nulls.SKIP)
    private void setGroup(EntityGroup group) {
        this.group = group;
    }
    
    public EntityGroup getGroup() {
        return group;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setLoot(List<EntityLoot> loot) {
        this.loot = new WeightedMap<>(loot, EntityLoot::getFrequency);
    }
    
    public WeightedMap<EntityLoot> getLoot() {
        return loot;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setPlacedLoot(List<EntityLoot> placedLoot) {
        this.placedLoot = new WeightedMap<>(placedLoot, EntityLoot::getFrequency);
    }
    
    public WeightedMap<EntityLoot> getPlacedLoot() {
        return placedLoot;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setLootByWeapon(Map<Item, List<EntityLoot>> lootByWeapon) {
        this.lootByWeapon = lootByWeapon.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey, entry -> new WeightedMap<EntityLoot>(entry.getValue(), EntityLoot::getFrequency)));
    }
    
    public Map<Item, WeightedMap<EntityLoot>> getLootByWeapon() {
        return lootByWeapon;
    }
    
    @JsonSetter(value = "defense", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setResistances(Map<DamageType, Float> resistances) {
        this.resistances = resistances;
    }
    
    public Map<DamageType, Float> getResistances() {
        return resistances;
    }
    
    @JsonSetter(value = "weakness", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setWeaknesses(Map<DamageType, Float> weaknesses) {
        this.weaknesses = weaknesses;
    }
    
    public Map<DamageType, Float> getWeaknesses() {
        return weaknesses;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setComponents(Map<String, String[]> components) {
        this.components = components;
    }
    
    public Map<String, String[]> getComponents() {
        return components;
    }
    
    @JsonSetter(value = "set_attachments", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
    
    public Map<String, String> getAttachments() {
        return attachments;
    }
    
    @JsonSetter(value = "attachments", nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setAttachmentTypes(List<String> attachmentTypes) {
        this.attachmentTypes = attachmentTypes;
    }
    
    public List<String> getAttachmentTypes() {
        return attachmentTypes;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setSlots(List<String> slots) {
        this.slots = slots;
    }
    
    public List<String> getSlots() {
        return slots;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setBehavior(List<Map<String, Object>> behavior) {
        this.behavior = behavior;
    }
    
    public List<Map<String, Object>> getBehavior() {
        return behavior;
    }
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private void setAnimations(List<Map<String, Object>> animations) {
        this.animations = animations.stream()
                .map(animation -> MapHelper.getString(animation, "name"))
                .collect(Collectors.toList());
    }
    
    public List<String> getAnimations() {
        return animations;
    }
}
