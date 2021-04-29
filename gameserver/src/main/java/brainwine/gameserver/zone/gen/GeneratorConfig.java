package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.zone.gen.models.BaseResourceType;
import brainwine.gameserver.zone.gen.models.CaveDecorator;
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
    
    @JsonProperty("stone_variants")
    private Map<StoneVariant, Integer> stoneVariants = new HashMap<>();
    
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
    
    public Map<StoneVariant, Integer> getStoneVariants() {
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
