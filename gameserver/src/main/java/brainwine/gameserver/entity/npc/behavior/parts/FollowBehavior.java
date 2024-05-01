package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;

public class FollowBehavior extends Behavior {
    
    @JsonCreator
    public FollowBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        if(entity.hasTarget()) {
            entity.setDirection(entity.getTarget().getX() - entity.getX() > 0 ? FacingDirection.EAST : FacingDirection.WEST);
        }
        
        return true;
    }
}
