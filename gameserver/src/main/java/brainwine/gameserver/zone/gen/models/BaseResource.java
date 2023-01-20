package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResource {
    
    @JsonProperty("type")
    private BaseResourceType type;
    
    @JsonProperty("blocks_per_spawn")
    private int blocksPerSpawn = 2000;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    @JsonCreator
    private BaseResource(@JsonProperty(value = "type", required = true) BaseResourceType type) {
        this.type = type;
    }
    
    public BaseResourceType getType() {
        return type;
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
