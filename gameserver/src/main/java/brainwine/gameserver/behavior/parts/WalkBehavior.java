package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;

public class WalkBehavior extends Behavior {
    
    @JsonCreator
    public WalkBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        entity.move(entity.getDirection().getId(), 0, "walk");
        return true;
    }
    
    @Override
    public boolean canBehave() {
        FacingDirection direction = entity.getDirection();
        return entity.isOnGround(direction.getId()) && !entity.isBlocked(direction.getId(), 0);
    }
}
