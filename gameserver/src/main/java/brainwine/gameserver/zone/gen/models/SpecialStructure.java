package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import brainwine.gameserver.prefab.Prefab;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialStructure {
    
    @JsonProperty("prefab")
    private Prefab prefab;
    
    @JsonProperty("blocks_per_spawn")
    private int blocksPerSpawn = 2000 * 600;
    
    @JsonProperty("min")
    private int min;
    
    @JsonProperty("max")
    private int max = 1;
    
    @JsonCreator
    private SpecialStructure(@JsonProperty(value = "prefab", required = true) Prefab prefab) {
        this.prefab = prefab;
    }
    
    public Prefab getPrefab() {
        return prefab;
    }
    
    public int getBlocksPerSpawn() {
        return blocksPerSpawn;
    }
    
    public int getMin() {
        return min;
    }
    
    public int getMax() {
        return max;
    }
}
