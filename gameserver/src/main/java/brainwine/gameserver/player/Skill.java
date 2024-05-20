package brainwine.gameserver.player;

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
    
    public static Skill[] getAdvancedSkills() {
        return new Skill[] {AUTOMATA, COMBAT, ENGINEERING, HORTICULTURE, LUCK, SCIENCE, SURVIVAL};
    }
    
    public static Skill fromId(String id) {
        for(Skill value : values()) {
            if(value.getId().equalsIgnoreCase(id)) {
                return value;
            }
        }
        
        return null;
    }
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
