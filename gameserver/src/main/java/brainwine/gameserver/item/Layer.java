package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum Layer
{
    BASE,
    BACK,
    FRONT,
    LIQUID,
    
    @JsonEnumDefaultValue
    NONE;
}