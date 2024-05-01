package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.player.Player;

public class RandomlyTargetBehavior extends Behavior {
    
    protected int range = 20;
    protected boolean friendlyFire;
    protected boolean blockable = true;
    protected String animation;  
    protected long targetLockedAt;
    
    @JsonCreator
    public RandomlyTargetBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        long now = System.currentTimeMillis();
        
        if(now > targetLockedAt + 5000) {
            entity.setTarget(null);
        }
        
        if(!entity.hasTarget()) {
            Player target = entity.getZone().getRandomPlayerInRange(entity.getX(), entity.getY(), range);
            
            if(target != null && !target.isGodMode() && !target.isStealthy() && !target.isDead() && !entity.isOwnedBy(target) && (!blockable || entity.canSee(target))) {
                entity.setTarget(target);
                targetLockedAt = now;
            }
        }
        
        if(animation != null && entity.hasTarget()) {
            entity.setAnimation(animation);
        }
        
        return true;
    }
    
    public void setRange(int range) {
        this.range = range;
    }
    
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }
    
    public void setBlockable(boolean blockable) {
        this.blockable = blockable;
    }
    
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
