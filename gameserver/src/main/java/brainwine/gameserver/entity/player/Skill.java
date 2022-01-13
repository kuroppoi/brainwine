package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Skill {
    
    AGILITY,
    AUTOMATA,
    BUILDING,
    COMBAT,
    ENGINEERING,
    HORTICULTURE,
    LUCK,
    MINING,
    PERCEPTION,
    SCIENCE,
    STAMINA,
    SURVIVAL;
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
