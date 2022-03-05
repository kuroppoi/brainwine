package brainwine.gameserver.dialog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DialogAlignment {
    
    LEFT,
    RIGHT;
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
