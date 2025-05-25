package brainwine.gameserver.player;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContainerType {
    
    INVENTORY("i"),
    HOTBAR("h"),
    ACCESSORIES("a");
    
    private final String id;
    
    private ContainerType(String id) {
        this.id = id;
    }
    
    @JsonValue
    public String getId() {
        return id;
    }
}
