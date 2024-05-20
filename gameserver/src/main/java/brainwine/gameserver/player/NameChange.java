package brainwine.gameserver.player;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties({"new_name", "previous_name", "date"})
public class NameChange {
    
    private String newName;
    private String previousName;
    private OffsetDateTime date;
    
    @JsonCreator
    private NameChange() {}
    
    public NameChange(String newName, String previousName) {
        this.newName = newName;
        this.previousName = previousName;
        date = OffsetDateTime.now();
    }
    
    public String getNewName() {
        return newName;
    }
    
    public String getPreviousName() {
        return previousName;
    }
    
    public OffsetDateTime getDate() {
        return date;
    }
}
