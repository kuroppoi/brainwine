package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum NotificationType {
    
    ALERT(1),
    STANDARD(2),
    EMOTE(3),
    INVISIBLE_DIALOG(5),
    LARGE(6),
    ACCOMPLISHMENT(10),
    SYSTEM(11),
    REWARD(12),
    CHAT(20),
    LEVEL_UP(21),
    ACHIEVEMENT(22),
    WELCOME(333);
    
    private final int id;
    
    private NotificationType(int id) {
        this.id = id;
    }
    
    @EnumValue
    public int getId() {
        return id;
    }
}
