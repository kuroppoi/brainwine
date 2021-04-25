package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Biome {
    
    @JsonProperty("plain")
    @JsonEnumDefaultValue
    PLAIN,
    
    @JsonProperty("arctic")
    ARCTIC,
    
    @JsonProperty("hell")
    HELL,
    
    @JsonProperty("desert")
    DESERT,
    
    @JsonProperty("brain")
    BRAIN,
    
    @JsonProperty("deep")
    DEEP,
    
    @JsonProperty("space")
    SPACE,
    
    @JsonProperty("ocean")
    OCEAN,
    
    EMPTY;
}
