package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum ChatType {
    
    CHAT("c"),
    EMOTE("e"),
    SPEECH("s"),
    THOUGHT("t");
    
    private final String id;
    
    private ChatType(String id) {
        this.id = id;
    }
    
    @EnumValue
    public String getId() {
        return id;
    }
}
