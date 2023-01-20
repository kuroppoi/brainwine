package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum TerrainType {
    
    @JsonEnumDefaultValue
    NORMAL,
    FILLED,
    ASTEROIDS;
}
