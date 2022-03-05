package brainwine.gameserver.dialog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DialogType {
    
    STANDARD,
    ANDROID,
    LOOT,
    LOOT_RED,
    LOOT_MECH;
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
