package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.item.Item;

// TODO groups
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitySpawn {
    
    @JsonProperty("entity")
    private EntityConfig entity;
    
    @JsonProperty("locale")
    private String locale;
    
    @JsonProperty("min_depth")
    private double minDepth;
    
    @JsonProperty("max_depth")
    private double maxDepth = 1;

    @JsonProperty("min_acidity")
    private double minAcidity = 0.0;

    @JsonProperty("max_acidity")
    private double maxAcidity = 1.0;
    
    @JsonProperty("orifice")
    private Item orifice;
    
    @JsonProperty("frequency")
    private double frequency = 1;
    
    public EntityConfig getEntity() {
        return entity;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public double getMinDepth() {
        return minDepth;
    }
    
    public double getMaxDepth() {
        return maxDepth;
    }

    public double getMinAcidity() {
        return minAcidity;
    }

    public double getMaxAcidity() {
        return maxAcidity;
    }
    
    public Item getOrifice() {
        return orifice;
    }
    
    public double getFrequency() {
        return frequency;
    }

}
