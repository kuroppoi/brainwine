package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.npc.Npc;

public class FallBehavior extends Behavior {
    
    @JsonCreator
    public FallBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        entity.move(0, 1, entity.getSpeed() + 0.5F, "fall");
        return true;
    }
    
    @Override
    public boolean canBehave() {
        return !entity.isOnGround();
    }
}
