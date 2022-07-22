package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 6, collection = true)
public class EntityPositionMessage extends Message {
    
    public int id;
    public int x;
    public int y;
    public int velocityX;
    public int velocityY;
    public FacingDirection direction;
    public int targetX;
    public int targetY;
    public int animation;
    
    public EntityPositionMessage(Entity entity) {
        this(entity.getId(), entity.getX(), entity.getY(), entity.getVelocityX(), entity.getVelocityY(), entity.getDirection(), entity.getTargetX(), entity.getTargetY(), entity.getAnimation());
    }
    
    public EntityPositionMessage(int id, float x, float y, float velocityX, float velocityY, FacingDirection direction, int targetX, int targetY, int animation) {
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
}
