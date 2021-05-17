package brainwine.gameserver.command;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum NotificationType {
    
    ALERT(1),
    EMOTE(3),
    WELCOME(6),
    SYSTEM(11),
    PM(19),
    CHAT(20),
    LEVEL_UP(21),
    ACHIEVEMENT(22),
    WELCOME_IOS(333);
    
    private final int id;
    
    private NotificationType(int id) {
        this.id = id;
    }
    
    @EnumValue
    public int getId() {
        return id;
    }
}
