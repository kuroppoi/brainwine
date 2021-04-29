package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.zone.gen.GeneratorContext;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CaveDecorator {
    
    @JsonProperty("min_size")
    private int minSize = 20;
    
    @JsonProperty("max_size")
    private int maxSize = 8000;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    @JsonProperty("frequency")
    private int frequency = 1;
    
    public abstract void decorate(GeneratorContext ctx, Cave cave);
    
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
    
    public int getFrequency() {
        return frequency;
    }
}
