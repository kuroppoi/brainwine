package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSetter;

import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Vector2i;

public class EruptionAttackBehavior extends Behavior {
    
    protected EntityConfig entityConfig;
    protected float speed = 8;
    protected float frequency = 1;
    protected float range = 10;
    protected Object burst;
    protected long lastAttackAt;
    
    @JsonCreator
    public EruptionAttackBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        lastAttackAt = System.currentTimeMillis();
        
        // Fail is no bullet entity is defined
        if(entityConfig == null) {
            return false;
        }
        
        Vector2i targetPosition = null;
        
        // If entity is mounted, projectile direction depends on which direction the mount block is facing
        // Otherwise, it'll fire the projectile upwards
        if(entity.isMounted()) {
            Vector2i mountBlock = entity.getMountBlock();
            int x = mountBlock.getX();
            int y = mountBlock.getY();
            int mod = entity.getZone().getBlock(x, y).getFrontMod();
            int directionX = mod == 1 ? 1 : mod == 3 ? -1 : 0;
            int directionY = mod == 0 ? -1 : mod == 2 ? 1 : 0;
            targetPosition = new Vector2i((int)(x + range * directionX), (int)(y + range * directionY));
            Vector2i hitBlockPosition = entity.getZone().raycast(x, y, targetPosition.getX(), targetPosition.getY());
            
            // If a block is between the entity and the target, make that the target position instead
            if(hitBlockPosition != null) {
                targetPosition = hitBlockPosition;
            }
        } else {
            targetPosition = new Vector2i((int)entity.getX(), (int)(entity.getY() + range * -1));
        }
        
        // Prepare projectile details
        Map<String, Object> details = MapHelper.map(String.class, Object.class,
                "<", entity.getId(),
                ">", new int[] {targetPosition.getX(), targetPosition.getY()},
                "*", true,
                "s", speed);
        
        if(burst != null) {
            details.put("#", burst);
        }
        
        // Let 'er rip!
        Npc projectile = new Npc(entity.getZone(), entityConfig);
        projectile.setProperties(details);
        entity.sendMessageToTrackers(new EntityStatusMessage(projectile, EntityStatus.ENTERING));
        return true;
    }
    
    @Override
    public boolean canBehave() {
        return System.currentTimeMillis() - lastAttackAt >= 1.0F / frequency * 1000;
    }
    
    @JsonSetter("entity")
    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
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
