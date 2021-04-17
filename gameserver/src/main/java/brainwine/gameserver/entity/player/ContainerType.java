package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumIdentifier;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum ContainerType {
    
    HOTBAR("h"),
    ACCESSORIES("a");
    
    @EnumIdentifier
    public final String id;
    
    private ContainerType(String id) {
        this.id = id;
    }
}
