package brainwine.gameserver.entity;

import brainwine.gameserver.msgpack.EnumIdentifier;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum FacingDirection {
    
    WEST(-1),
    EAST(1);
    
    @EnumIdentifier
    public final int id;
    
    private FacingDirection(int id) {
        this.id = id;
    }
}
