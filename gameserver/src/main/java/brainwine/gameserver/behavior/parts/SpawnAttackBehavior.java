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
import brainwine.gameserver.util.Vector2i;

public class SpawnAttackBehavior extends Behavior {
    
    protected EntityConfig entityConfig;
    protected boolean npc;
    protected int max = 1;
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
        lastAttackAt = System.currentTimeMillis();
        
        // Fail is no entity config is defined
        if(entityConfig == null) {
            return false;
        }
        
        if(npc) {
            // Spawn child at parent's location
            Vector2i size = entity.getSize();
            int spawnX = (int)(entity.getX() + (size.getX() / 2F));
            int spawnY = (int)(entity.getY() + (size.getY() / 2F));
            Npc child = new Npc(entity.getZone(), entityConfig);
            child.setOwner(entity);
            entity.addChild(child);
            entity.getZone().spawnEntity(child, (int)spawnX, (int)spawnY, true);
        } else {
            // Prepare projectile details
            Map<String, Object> details = MapHelper.map(String.class, Object.class,
                    "<", entity.getId(),
                    ">", entity.getTarget().getId(),
                    "*", true,
                    "s", speed);
            
            if(burst != null) {
                details.put("#", burst);
            }
            
            // Spawn the projectile
            Npc projectile = new Npc(entity.getZone(), entityConfig);
            projectile.setProperties(details);
            entity.sendMessageToTrackers(new EntityStatusMessage(projectile, EntityStatus.ENTERING));
        }
        
        return true;
    }
    
    @Override
    public boolean canBehave() {
        return System.currentTimeMillis() - lastAttackAt >= 1.0F / frequency * 1000
                && entity.hasTarget() && entity.inRange(entity.getTarget(), 15)
                && (!npc || entity.getChildCount() < max);
    }
    
    @JsonSetter("entity")
    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }
    
    public void setNpc(boolean npc) {
        this.npc = npc;
    }
    
    public void setMax(int max) {
        this.max = max;
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
