package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;

public class WalkBehavior extends Behavior {
    
    protected String animation = "walk";
    
    @JsonCreator
    public WalkBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        entity.move(entity.getDirection().getId(), 0, animation);
        return true;
    }
    
    @Override
    public boolean canBehave() {
        FacingDirection direction = entity.getDirection();
        return entity.isOnGround(direction.getId()) && !entity.isBlocked(direction.getId(), 0);
    }
    
    @JsonAlias("walk_animation")
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
