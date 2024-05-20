package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.models.EntityStatusData;

@MessageInfo(id = 7, prepacked = true)
public class EntityStatusMessage extends Message {
    
    public Collection<EntityStatusData> statuses;
    
    public EntityStatusMessage(Collection<EntityStatusData> statuses) {
        this.statuses = statuses;
    }
    
    public EntityStatusMessage(Collection<? extends Entity> entities, EntityStatus status) {
        this(entities.stream().map(entity -> new EntityStatusData(entity, status)).collect(Collectors.toList()));
    }
    
    public EntityStatusMessage(Entity entity, EntityStatus status) {
        this(Arrays.asList(new EntityStatusData(entity, status)));
    }
    
    public EntityStatusMessage(Entity entity, EntityStatus status, Map<String, Object> details) {
        this(Arrays.asList(new EntityStatusData(entity, status, details)));
    }
    
    public EntityStatusMessage(int id, int type, String name, EntityStatus status, Map<String, Object> details) {
        this(Arrays.asList(new EntityStatusData(id, type, name, status, details)));
    }
}
