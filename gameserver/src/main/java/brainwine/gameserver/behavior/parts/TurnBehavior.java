package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;

public class TurnBehavior extends Behavior {
    
    @JsonCreator
    public TurnBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        entity.setDirection(entity.getDirection() == FacingDirection.WEST ? FacingDirection.EAST : FacingDirection.WEST);
        return false;
    }
    
    @Override
    public boolean canBehave() {
        return entity.isBlocked(entity.getDirection().getId(), 0);
    }
}
