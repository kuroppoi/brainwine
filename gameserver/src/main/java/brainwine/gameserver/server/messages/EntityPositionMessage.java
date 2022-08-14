package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.models.EntityPositionData;

@MessageInfo(id = 6, prepacked = true)
public class EntityPositionMessage extends Message {
    
    public Collection<EntityPositionData> positions;
    
    public EntityPositionMessage(Collection<? extends Entity> entities) {
        this.positions = entities.stream().map(EntityPositionData::new).collect(Collectors.toList());
    }
    
    public EntityPositionMessage(Entity entity) {
        this.positions = Arrays.asList(new EntityPositionData(entity));
    }
    
    public EntityPositionMessage(int id, float x, float y, float velocityX, float velocityY, FacingDirection direction,
            int targetX, int targetY, int animation) {
        this.positions = Arrays.asList(new EntityPositionData(id, x, y, velocityX, velocityY, direction, targetX, targetY, animation));
    }
}
