package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.models.BaseResourceType;
import brainwine.gameserver.zone.gen.models.Deposit;
import brainwine.gameserver.zone.gen.models.OreDeposit;
import brainwine.gameserver.zone.gen.models.StoneVariant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratorConfig {
    
    @JsonProperty("surface")
    private boolean surface = true;
    
    @JsonProperty("surface_fillers")
    private Item[] surfaceFillers = {};
    
    @JsonProperty("speleothems")
    private Item[] speleothems = {};
    
    @JsonProperty("unique_structures")
    private Prefab[] uniqueStructures = {};
    
    @JsonProperty("dungeons")
    private WeightedMap<Prefab> dungeons = new WeightedMap<>();
    
    @JsonProperty("spawn_towers")
    private WeightedMap<Prefab> spawnTowers = new WeightedMap<>();
    
    @JsonProperty("dungeon_region")
    private Vector2i dungeonRegion = new Vector2i(80, 64);
    
    @JsonProperty("dungeon_chance")
    private double dungeonRate = 0.25;
    
    @JsonProperty("stone_variants")
    private WeightedMap<StoneVariant> stoneVariants = new WeightedMap<>();
    
    @JsonProperty("cave_types")
    @JsonDeserialize(using = CaveDecoratorListDeserializer.class)
    private List<CaveDecorator> decorators = new ArrayList<>();
    
    @JsonProperty("base_resources")
    private Map<BaseResourceType, Deposit> baseResources = new HashMap<>();
    
    @JsonProperty("ore_deposits")
    private Map<Item, OreDeposit> oreDeposits = new HashMap<>();
    
    public boolean getSurface() {
        return surface;
    }
    
    public Item[] getSurfaceFillers() {
        return surfaceFillers;
    }
    
    public Item[] getSpeleothems() {
        return speleothems;
    }
    
    public Prefab[] getUniqueStructures() {
        return uniqueStructures;
    }
    
    public WeightedMap<Prefab> getDungeons() {
        return dungeons;
    }
    
    public WeightedMap<Prefab> getSpawnTowers() {
        return spawnTowers;
    }
    
    public Vector2i getDungeonRegion() {
        return dungeonRegion;
    }
    
    public double getDungeonRate() {
        return dungeonRate;
    }
    
    public WeightedMap<StoneVariant> getStoneVariants() {
        return stoneVariants;
    }
    
    public List<CaveDecorator> getCaveDecorators() {
        return decorators;
    }
    
    public Map<BaseResourceType, Deposit> getBaseResources() {
        return baseResources;
    }
    
    public Map<Item, OreDeposit> getOreDeposits() {
        return oreDeposits;
    }
}
