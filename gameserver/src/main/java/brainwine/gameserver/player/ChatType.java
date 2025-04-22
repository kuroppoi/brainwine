package brainwine.gameserver.player;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatType {
    
    CHAT("c"),
    EMOTE("e"),
    SPEECH("s"),
    THOUGHT("t"),
    PRIVATE("p");
    
    private final String id;
    
    private ChatType(String id) {
        this.id = id;
    }
    
    @JsonValue
    public String getId() {
        return id;
    }
}
