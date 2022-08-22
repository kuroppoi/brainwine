package brainwine.gameserver.behavior.parts;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSetter;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MapHelper;

public class SpawnAttackBehavior extends Behavior {
    
    protected EntityConfig bulletConfig;
    protected float speed = 8;
    protected float frequency = 1;
    protected float range = 15;
    protected Object burst;
    protected long lastAttackAt;
    
    @JsonCreator
    public SpawnAttackBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
     
    @Override
    public boolean behave() {
        // TODO much more to be done here
        lastAttackAt = System.currentTimeMillis();
        
        if(bulletConfig != null) {
            Npc bullet = new Npc(entity.getZone(), bulletConfig);
            Map<String, Object> details = MapHelper.map(String.class, Object.class,
                    "<", entity.getId(),
                    ">", entity.getTarget().getId(),
                    "*", true,
                    "s", speed);
            
            if(burst != null) {
                details.put("#", burst);
            }
            
            bullet.setProperties(details);
            entity.sendMessageToTrackers(new EntityStatusMessage(bullet, EntityStatus.ENTERING));
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean canBehave() {
        return System.currentTimeMillis() - lastAttackAt >= 1.0F / frequency * 1000
                && entity.hasTarget() && entity.inRange(entity.getTarget(), 15);
    }
    
    @JsonSetter("entity")
    public void setBulletConfig(EntityConfig bulletConfig) {
        this.bulletConfig = bulletConfig;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }
    
    public void setRange(float range) {
        this.range = range;
    }
    
    public void setBurst(Object burst) {
        this.burst = burst;
    }
}
