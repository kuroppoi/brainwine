package brainwine.gameserver.entity;

import brainwine.gameserver.msgpack.EnumIdentifier;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum EntityType {
    
    PLAYER(0),
    GHOST(1),
    TERRAPUS_JUVENLIE(3),
    TERRAPUS_ADULT(4);
    
    @EnumIdentifier
    public final int id;
    
    private EntityType(int id) {
        this.id = id;
    }
}
