package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.FacingDirection;

@JsonFormat(shape = Shape.ARRAY)
public class EntityPositionData {
    
    private final int id;
    private final int x;
    private final int y;
    private final int velocityX;
    private final int velocityY;
    private final FacingDirection direction;
    private final int targetX;
    private final int targetY;
    private final int animation;
    
    public EntityPositionData(Entity entity) {
        this(entity.getId(), entity.getX(), entity.getY(), entity.getVelocityX(), entity.getVelocityY(), entity.getDirection(),
                entity.getTargetX(), entity.getTargetY(), entity.getAnimation());
    }
    
    public EntityPositionData(int id, float x, float y, float velocityX, float velocityY, FacingDirection direction,
            int targetX, int targetY, int animation) {
        this.id = id;
        this.x = (int)(x * Entity.POSITION_MODIFIER);
        this.y = (int)(y * Entity.POSITION_MODIFIER);
        this.velocityX = (int)(velocityX * Entity.VELOCITY_MODIFIER);
        this.velocityY = (int)(velocityY * Entity.VELOCITY_MODIFIER);
        this.direction = direction;
        this.targetX = targetX * Entity.VELOCITY_MODIFIER;
        this.targetY = targetY * Entity.VELOCITY_MODIFIER;
        this.animation = animation;
    }
    
    public int getId() {
        return id;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getVelocityX() {
        return velocityX;
    }
    
    public int getVelocityY() {
        return velocityY;
    }
    
    public FacingDirection getDirection() {
        return direction;
    }
    
    public int getTargetX() {
        return targetX;
    }
    
    public int getTargetY() {
        return targetY;
    }
    
    public int getAnimation() {
        return animation;
    }
}