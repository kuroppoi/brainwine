package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.EntityType;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 7, collection = true)
public class EntityStatusMessage extends Message {
    
    public int id;
    public EntityType type;
    public String name;
    public EntityStatus status;
    public Map<String, Object> details;
    
    public EntityStatusMessage(Entity entity, EntityStatus status) {
        this(entity.getId(), entity.getType(), entity.getName(), status, entity.getStatusConfig());
    }
    
    public EntityStatusMessage(int id, EntityType type, String name, EntityStatus status, Map<String, Object> details) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.status = status;
        this.details = details;
    }
}
