package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    
    POPUP(1),
    SYSTEM(2),
    EMOTE(3),
    FANCY_EMOTE(4),
    INVISIBLE_DIALOG(5), // v2 only
    LARGE(6),
    ACCOMPLISHMENT(10),
    PEER_ACCOMPLISHMENT(11),
    REWARD(12), // v2 only
    CHAT(20),
    LEVEL_UP(21), // v3 only
    ACHIEVEMENT(22), // v3 only
    WELCOME(333), // v2 only
    MAINTENANCE(503); // v2 only
    
    private final int id;
    
    private NotificationType(int id) {
        this.id = id;
    }
    
    @JsonValue
    public int getId() {
        return id;
    }
}
