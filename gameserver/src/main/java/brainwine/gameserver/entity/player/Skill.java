package brainwine.gameserver.entity.player;

import brainwine.gameserver.msgpack.EnumIdentifier;
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
    
    @EnumIdentifier
    public String code() {
        return toString().toLowerCase();
    }
}
