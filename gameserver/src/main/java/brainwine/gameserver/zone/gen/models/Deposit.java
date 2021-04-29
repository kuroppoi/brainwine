package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Deposit {
    
    @JsonProperty("per")
    private int per = 2000;
    
    @JsonProperty("min_depth")
    private double minDepth = 0;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;
    
    public int getPer() {
        return per;
    }
    
    public double getMinDepth() {
        return minDepth;
    }
    
    public double getMaxDepth() {
        return maxDepth;
    }
}
