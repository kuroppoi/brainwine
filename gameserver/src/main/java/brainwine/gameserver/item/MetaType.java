package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum MetaType {
    
    LOCAL,
    GLOBAL,
    HIDDEN,
    
    @JsonEnumDefaultValue
    NONE;
}
