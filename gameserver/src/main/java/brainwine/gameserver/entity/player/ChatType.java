package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumIdentifier;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum ChatType {
    
    CHAT("c"),
    EMOTE("e"),
    SPEECH("s"),
    THOUGHT("t");
    
    @EnumIdentifier
    public String id;
    
    private ChatType(String id) {
        this.id = id;
    }
}
