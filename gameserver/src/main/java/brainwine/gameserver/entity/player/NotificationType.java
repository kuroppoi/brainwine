package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonValue;

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
    
    @JsonValue
    public int getId() {
        return id;
    }
}
