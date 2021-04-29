package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OreDeposit extends Deposit {
    
    @JsonProperty("min_size")
    private int minSize = 3;
    
    @JsonProperty("max_size")
    private int maxSize = 11;
    
    public int getMinSize() {
        return minSize;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
}
