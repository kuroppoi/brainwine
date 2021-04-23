package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum ContainerType {
    
    HOTBAR("h"),
    ACCESSORIES("a");
    
    private final String id;
    
    private ContainerType(String id) {
        this.id = id;
    }
    
    @EnumValue
    public String getId() {
        return id;
    }
}
