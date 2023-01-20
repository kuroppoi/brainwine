package brainwine.gameserver.zone.gen.caves;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaveType {
    
    @JsonProperty("min_size")
    private int minSize = 20;
    
    @JsonProperty("max_size")
    private int maxSize = 8000;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    @JsonProperty("frequency")
    private double frequency = 1;
    
    @JsonProperty("decorators")
    private List<CaveDecorator> decorators = new ArrayList<>();
    
    @JsonCreator
    private CaveType() {}
    
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
    
    public double getFrequency() {
        return frequency;
    }
    
    public List<CaveDecorator> getDecorators() {
        return decorators;
    }
}
