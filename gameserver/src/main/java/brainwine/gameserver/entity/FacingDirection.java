package brainwine.gameserver.entity;

import brainwine.gameserver.msgpack.DefaultEnumValue;
import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum FacingDirection {
    
    @DefaultEnumValue
    WEST(-1),
    EAST(1);
    
    private final int id;
    
    private FacingDirection(int id) {
        this.id = id;
    }
    
    @EnumValue
    public int getId() {
        return id;
    }
}
