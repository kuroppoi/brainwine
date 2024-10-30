package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.item.Item;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonMappingException;

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

    private double normalizeAcidity(Object object) throws JsonMappingException {
        if("purification threshold".equals(object)) {
            return 0.05;
        } else if(object instanceof Number) {
            return ((Number) object).doubleValue();
        }

        throw new JsonMappingException("Unknown acidity value");
    }

    @JsonSetter
    public void setMinAcidity(Object minAcidity) throws JsonMappingException {
        this.minAcidity = normalizeAcidity(minAcidity);
    }

    @JsonSetter
    public void setMaxAcidity(Object maxAcidity) throws JsonMappingException {
        this.maxAcidity = normalizeAcidity(maxAcidity);
    }

}
