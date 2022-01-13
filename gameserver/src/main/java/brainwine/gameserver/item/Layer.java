package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Layer {
    
    BASE,
    BACK,
    FRONT,
    LIQUID,
    
    @JsonEnumDefaultValue
    NONE;
}