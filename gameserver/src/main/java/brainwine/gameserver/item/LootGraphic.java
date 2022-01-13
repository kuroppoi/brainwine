package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LootGraphic {
    
    LOOT,
    LOOT_RED,
    LOOT_MECH,

    @JsonEnumDefaultValue
    NONE;
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
