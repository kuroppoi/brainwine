package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ColorSlot {
    
    SKIN_COLOR("c*"),
    HAIR_COLOR("h*");
    
    private final String id;
    
    private ColorSlot(String id) {
        this.id = id;
    }
    
    @JsonCreator
    public static ColorSlot fromId(String id) {
        for(ColorSlot value : values()) {
            if(value.getId().equals(id)) {
                return value;
            }
        }
        
        return null;
    }
    
    @JsonValue
    public String getId() {
        return id;
    }
}
