package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Action {
    
    DIG,
    HEAL,
    REFILL,
    
    @JsonEnumDefaultValue
    NONE;
}
