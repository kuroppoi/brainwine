package brainwine.gameserver.server.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;

@JsonFormat(shape = Shape.ARRAY)
public class EntityStatusData {
    
    private final int id;
    private final int type;
    private final String name;
    private final EntityStatus status;
    private final Map<String, Object> details;
    
    public EntityStatusData(Entity entity, EntityStatus status) {
        this(entity, status, entity.getStatusConfig());
    }
    
    public EntityStatusData(Entity entity, EntityStatus status, Map<String, Object> details) {
        this(entity.getId(), entity.getType(), entity.getName(), status, details);
    }
    
    public EntityStatusData(int id, int type, String name, EntityStatus status, Map<String, Object> details) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.status = status;
        this.details = details;
    }
    
    public static EntityStatusData entering(Entity entity) {
        return new EntityStatusData(entity, EntityStatus.ENTERING);
    }
    
    public static EntityStatusData exiting(Entity entity) {
        return new EntityStatusData(entity, EntityStatus.EXITING);
    }
    
    public int getId() {
        return id;
    }
    
    public int getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public EntityStatus getStatus() {
        return status;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
}
