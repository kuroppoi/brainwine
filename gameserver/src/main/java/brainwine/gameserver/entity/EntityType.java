package brainwine.gameserver.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EntityType {
    
    PLAYER(0),
    GHOST(1),
    TERRAPUS_JUVENLIE(3),
    TERRAPUS_ADULT(4);
    
    private final int id;
    
    private EntityType(int id) {
        this.id = id;
    }
    
    @JsonValue
    public int getId() {
        return id;
    }
}
