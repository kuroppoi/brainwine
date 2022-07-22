package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.Vector2i;

public class FlyTowardBehavior extends FlyBehavior {
    
    protected long lastNearbyAt;
    
    @JsonCreator
    public FlyTowardBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean canBehave() {
        return entity.hasTarget() && !shouldBackOff();
    }
    
    @Override
    protected float getSpeedMultiplier() {
        return 1.25F;
    }
    
    @Override
    protected Vector2i getTargetPoint() {
        return new Vector2i((int)entity.getTarget().getX(), (int)entity.getTarget().getY());
    }
    
    protected boolean shouldBackOff() {
        long now = System.currentTimeMillis();
        
        if(now < lastNearbyAt + 2000) {
            return true;
        }
        
        Entity target = entity.getTarget();
        
        if(target != null && entity.inRange(target, 2)) {
            lastNearbyAt = now;
            return true;
        }
        
        return false;
    }
}
