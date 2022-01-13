package brainwine.gameserver.entity;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FacingDirection {
    
    @JsonEnumDefaultValue
    WEST(-1),
    EAST(1);
    
    private final int id;
    
    private FacingDirection(int id) {
        this.id = id;
    }
    
    @JsonValue
    public int getId() {
        return id;
    }
}
