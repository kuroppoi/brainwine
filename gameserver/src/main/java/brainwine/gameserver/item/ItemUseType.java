package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ItemUseType {
    
    AFTERBURNER,
    CONTAINER,
    CREATE_DIALOG,
    DIALOG,
    GUARD,
    CHANGE,
    FIELDABLE,
    FLY,
    MULTI,
    PROTECTED,
    PUBLIC,
    SWITCH,
    SWITCHED,
    TELEPORT,
    ZONE_TELEPORT,
    
    @JsonEnumDefaultValue
    UNKNOWN;
        
    @JsonCreator
    public static ItemUseType fromId(String id) {
        String formatted = id.toUpperCase().replace(" ", "_");
        
        for(ItemUseType value : values()) {
            if(value.toString().equals(formatted)) {
                return value;
            }
        }
        
        return UNKNOWN;
    }
}
