package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 2, json = true, compressed = true)
public class ConfigurationMessage extends Message {
    
    public String entityId;
    public Map<String, Object> playerHash;
    public Map<String, Object> configHash;
    public Map<String, Object> zoneHash;
    
    public ConfigurationMessage(int entityId, Map<String, Object> playerHash, Map<String, Object> configHash, Map<String, Object> zoneHash) {
        this.entityId = String.valueOf(entityId);
        this.playerHash = playerHash;
        this.configHash = configHash;
        this.zoneHash = zoneHash;
    }
}
