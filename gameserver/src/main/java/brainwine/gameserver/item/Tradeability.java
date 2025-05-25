package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Tradeability {
    
    TRUE,
    FALSE,
    LEVELED;
    
    @JsonCreator
    private static Tradeability create(String string) {
        switch(string) {
        default:
            return TRUE;
        case "false":
            return FALSE;
        case "leveled":
            return LEVELED;
        }
    }
}
