package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClothingSlot {
    
    HAIR("h"),
    FACIAL_HAIR("fh"),
    TOPS("t"),
    BOTTOMS("b"),
    FOOTWEAR("fw"),
    HEADGEAR("hg"),
    FACIAL_GEAR("fg"),
    SUIT("u"),
    TOPS_OVERLAY("to"),
    ARMS_OVERLAY("ao"),
    LEGS_OVERLAY("lo"),
    FOOTWEAR_OVERLAY("fo");
    
    private final String id;
    
    private ClothingSlot(String id) {
        this.id = id;
    }
    
    @JsonCreator
    public static ClothingSlot fromId(String id) {
        for(ClothingSlot value : values()) {
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
