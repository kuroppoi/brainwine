package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ModType {
    
    DECAY,
    ROTATION,
    
    @JsonEnumDefaultValue
    NONE;
}
