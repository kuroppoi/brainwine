package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;

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
    
    @Override
    public boolean isJson() {
        return true;
    }
    
    @Override
    public boolean isCompressed() {
        return true;
    }
}
