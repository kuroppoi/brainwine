package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum InventoryType {
    
    ACCESSORY,
    HIDDEN,
    
    @JsonEnumDefaultValue
    NONE,
}
