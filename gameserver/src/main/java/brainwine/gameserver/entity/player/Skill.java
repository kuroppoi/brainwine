package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
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
    
    @EnumValue
    public String getId() {
        return toString().toLowerCase();
    }
}
