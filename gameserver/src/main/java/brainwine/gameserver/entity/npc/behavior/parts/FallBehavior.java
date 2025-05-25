package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;

public class FallBehavior extends Behavior {
    
    protected String animation = "fall";
    
    @JsonCreator
    public FallBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        entity.move(0, 1, entity.getSpeed() + 0.5F, animation);
        return true;
    }
    
    @Override
    public boolean canBehave() {
        return !entity.isOnGround();
    }
    
    @JsonAlias("fall_animation")
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
