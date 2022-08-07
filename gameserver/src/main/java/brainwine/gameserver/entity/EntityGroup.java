package brainwine.gameserver.entity;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EntityGroup {
    
    ANDROID,
    AUTOMATA,
    BRAINS,
    CREATURE,
    REVENANT,
    ROBOT,
    SUPERNATURAL,
    TURRET,
    
    @JsonEnumDefaultValue
    NONE;
}
