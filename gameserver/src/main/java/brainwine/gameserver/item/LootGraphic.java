package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum LootGraphic {
    
    LOOT,
    LOOT_RED,
    LOOT_MECH,

    @JsonEnumDefaultValue
    NONE;
    
    @EnumValue
    public String getId() {
        return toString().toLowerCase();
    }
}
