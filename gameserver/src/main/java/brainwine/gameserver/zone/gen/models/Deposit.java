package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.WeightedMap;

public class Deposit {
    
    @JsonProperty("items")
    private WeightedMap<Item> items;
    
    @JsonProperty("blocks_per_spawn")
    private int blocksPerSpawn = 2000;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    @JsonCreator
    private Deposit(@JsonProperty(value = "items", required = true) WeightedMap<Item> items) {
        this.items = items;
    }
    
    public WeightedMap<Item> getItems() {
        return items;
    }
    
    public int getBlocksPerSpawn() {
        return blocksPerSpawn;
    }
    
    public double getMinDepth() {
        return minDepth;
    }
    
    public double getMaxDepth() {
        return maxDepth;
    }
}
