package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum KarmaLevel {
    
    @JsonProperty("Godly")
    GODLY(500),
    
    @JsonProperty("Angelic")
    ANGELIC(250),
    
    @JsonProperty("Great")
    GREAT(100),
    
    @JsonProperty("Good")
    GOOD(50),
    
    @JsonProperty("Neutral")
    NEUTRAL(0),
    
    @JsonProperty("Fair")
    FAIR(-50),
    
    @JsonEnumDefaultValue
    @JsonProperty("Poor")
    POOR(-200);
    
    private final int karma;
    
    private KarmaLevel(int karma) {
        this.karma = karma;
    }
    
    public int getKarma() {
        return karma;
    }
}
