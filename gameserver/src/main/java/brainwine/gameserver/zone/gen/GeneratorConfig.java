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
import brainwine.gameserver.util.WeightedList;
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
    private WeightedList<Prefab> dungeons = new WeightedList<>();
    
    @JsonProperty("spawn_towers")
    private WeightedList<Prefab> spawnTowers = new WeightedList<>();
    
    @JsonProperty("dungeon_region")
    private Vector2i dungeonRegion = new Vector2i(80, 64);
    
    @JsonProperty("dungeon_chance")
    private double dungeonRate = 0.25;
    
    @JsonProperty("stone_variants")
    private WeightedList<StoneVariant> stoneVariants = new WeightedList<>();
    
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
    
    public WeightedList<Prefab> getDungeons() {
        return dungeons;
    }
    
    public WeightedList<Prefab> getSpawnTowers() {
        return spawnTowers;
    }
    
    public Vector2i getDungeonRegion() {
        return dungeonRegion;
    }
    
    public double getDungeonRate() {
        return dungeonRate;
    }
    
    public WeightedList<StoneVariant> getStoneVariants() {
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
