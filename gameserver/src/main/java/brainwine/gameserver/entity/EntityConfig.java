package brainwine.gameserver.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.Vector2i;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityConfig {
    
    @JsonProperty("code")
    private int type;
    
    @JacksonInject("name") 
    private String name;
    
    @JsonProperty("health")
    private float maxHealth = 5;
    
    @JsonProperty("speed")
    private float baseSpeed = 3;
    
    @JsonProperty("size")
    private Vector2i size = new Vector2i(1, 1);
    
    @JsonProperty("loot")
    private List<EntityLoot> loot = new ArrayList<>();
    
    @JsonProperty("loot_by_weapon")
    private Map<Item, List<EntityLoot>> lootByWeapon = new HashMap<>();
    
    @JsonProperty("defense")
    private Map<DamageType, Float> resistances = new HashMap<>();
    
    @JsonProperty("weakness")
    private Map<DamageType, Float> weaknesses = new HashMap<>();
    
    @JsonProperty("components")
    private Map<String, String[]> components = Collections.emptyMap();
    
    @JsonProperty("set_attachments")
    private Map<String, String> attachments = Collections.emptyMap();
    
    @JsonProperty("behavior")
    private List<Map<String, Object>> behavior = Collections.emptyList();
    
    @JsonProperty("animations")
    private List<Map<String, Object>> animations = Collections.emptyList();
    
    @JsonProperty("slots")
    private List<String> slots = Collections.emptyList();
    
    @JsonProperty("attachments")
    private List<String> possibleAttachments = Collections.emptyList();
    
    @JsonCreator
    private EntityConfig() {}
    
    @JsonCreator
    public static EntityConfig fromName(String name) {
        return EntityRegistry.getEntityConfig(name);
    }
    
    public int getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public float getMaxHealth() {
        return maxHealth;
    }
    
    public float getBaseSpeed() {
        return baseSpeed;
    }
    
    public Vector2i getSize() {
        return size;
    }
    
    public List<EntityLoot> getLoot() {
        return loot;
    }
    
    public Map<Item, List<EntityLoot>> getLootByWeapon() {
        return lootByWeapon;
    }
    
    public Map<DamageType, Float> getResistances() {
        return resistances;
    }
    
    public Map<DamageType, Float> getWeaknesses() {
        return weaknesses;
    }
    
    public Map<String, String[]> getComponents() {
        return components;
    }
    
    public Map<String, String> getAttachments() {
        return attachments;
    }
    
    public List<Map<String, Object>> getBehavior() {
        return behavior;
    }
    
    public List<Map<String, Object>> getAnimations() {
        return animations;
    }
    
    public List<String> getSlots() {
        return slots;
    }
    
    public List<String> getPossibleAttachments() {
        return possibleAttachments;
    }
}
