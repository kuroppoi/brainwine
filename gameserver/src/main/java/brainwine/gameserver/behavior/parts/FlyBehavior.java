package brainwine.gameserver.behavior.parts;

import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.Vector2i;

public class FlyBehavior extends Behavior {
    
    protected boolean blockable = true;
    protected String animation = "fly";
    protected Vector2i targetPoint;
    
    @JsonCreator
    public FlyBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        if((targetPoint = getTargetPoint()) != null) {
            Vector2i next = entity.getZone().raynext((int)entity.getX(), (int)entity.getY(), targetPoint.getX(), targetPoint.getY());
            
            if(next != null) {
                int moveX = (int)(next.getX() - entity.getX());
                int moveY = (int)(next.getY() - entity.getY());
                
                if(!blockable || !entity.isBlocked(moveX, moveY)) {
                    entity.move(moveX, moveY, entity.getBaseSpeed() * getSpeedMultiplier(), animation);
                    return true;
                }
            }
        }
        
        targetPoint = null;
        return false;
    }
    
    protected float getSpeedMultiplier() {
        return 1;
    }
    
    protected Vector2i getTargetPoint() {
        return targetPoint == null ? getRandomPoint(10, 30) : targetPoint;
    }
    
    protected Vector2i getRandomPoint(int minDistance, int maxDistance) {
        int distance = ThreadLocalRandom.current().nextInt(minDistance, maxDistance);
        double theta = Math.random() * 2 * Math.PI;
        int x = (int)(entity.getX() + distance * Math.cos(theta));
        int y = (int)(entity.getY() + distance * Math.sin(theta));
        return new Vector2i(x, y);
    }
    
    public void setBlockable(boolean blockable) {
        this.blockable = blockable;
    }
    
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
