package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum KarmaLevel {
    
    GODLY("Godly", 500),
    ANGELIC("Angelic", 250),
    GREAT("Great", 100),
    GOOD("Good", 50),
    NEUTRAL("Neutral", 0),
    FAIR("Fair", -50),
    
    @JsonEnumDefaultValue
    POOR("Poor", -200);
    
    private final String id;
    private final int karma;
    
    private KarmaLevel(String id, int karma) {
        this.id = id;
        this.karma = karma;
    }
    
    @JsonValue
    @EnumValue
    public String getId() {
        return id;
    }
    
    public int getKarma() {
        return karma;
    }
}
