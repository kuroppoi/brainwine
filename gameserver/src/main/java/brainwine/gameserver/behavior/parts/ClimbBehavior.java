package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;

public class ClimbBehavior extends Behavior {
    
    protected int lastClimbSide = 1;
    
    @JsonCreator
    public ClimbBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        if(climb(lastClimbSide)) {
            return true;
        }
        
        return climb(lastClimbSide * -1);
    }
    
    protected boolean climb(int side) {
        FacingDirection direction = entity.getDirection();
        int y = side * direction.getId() * -1;
        
        if((entity.isBlocked(side, 0) || entity.isBlocked(side, y)) && !entity.isBlocked(0, y)) {
            lastClimbSide = side;
            entity.move(0, y, entity.getBaseSpeed() * 0.75F, side == -1 ? "climb-left" : "climb-right");
            return true;
        }
        
        return false;
    }
}
