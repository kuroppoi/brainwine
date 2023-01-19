package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OreDeposit {
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("blocks_per_spawn")
    private int blocksPerSpawn = 2000;
    
    @JsonProperty("min_size")
    private int minSize = 3;
    
    @JsonProperty("max_size")
    private int maxSize = 11;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    @JsonCreator
    private OreDeposit(@JsonProperty(value = "item", required = true) Item item) {
        this.item = item;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getBlocksPerSpawn() {
        return blocksPerSpawn;
    }
    
    public int getMinSize() {
        return minSize;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
    
    public double getMinDepth() {
        return minDepth;
    }
    
    public double getMaxDepth() {
        return maxDepth;
    }
}
