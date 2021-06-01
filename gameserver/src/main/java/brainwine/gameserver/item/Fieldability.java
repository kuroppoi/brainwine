package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Fieldability {
    
    TRUE,
    FALSE,
    PLACED;
    
    @JsonCreator
    private static Fieldability create(String string) {
        switch(string) {
        default:
            return TRUE;
        case "false":
            return FALSE;
        case "placed":
            return PLACED;
        }
    }
}
