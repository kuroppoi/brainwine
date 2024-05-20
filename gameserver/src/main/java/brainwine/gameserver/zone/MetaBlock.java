package brainwine.gameserver.zone;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;

/**
 * I hate this class and everything in it.
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaBlock {
    
    @JsonProperty("x")
    private int x;
    
    @JsonProperty("y")
    private int y;
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("owner")
    private String owner;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();
    
    @JsonCreator
    private MetaBlock() {}
    
    public MetaBlock(int x, int y) {
        this(x, y, Item.AIR);
    }
    
    public MetaBlock(int x, int y, Item item) {
        this(x, y, item, new HashMap<>());
    }
    
    public MetaBlock(int x, int y, Item item, Map<String, Object> metadata) {
        this(x, y, item, null, metadata);
    }
    
    public MetaBlock(int x, int y, Item item, Player owner, Map<String, Object> metadata) {
        this.x = x;
        this.y = y;
        this.item = item;
        setOwner(owner);
        this.metadata = metadata;
    }
    
    public void setOwner(Player owner) {
        this.owner = owner == null ? null : owner.getDocumentId();
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public boolean hasOwner() {
        return owner != null;
    }
    
    public boolean isOwnedBy(Player player) {
        return player != null && player.getDocumentId().equals(owner);
    }
    
    @JsonIgnore
    public Player getOwner() {
        return GameServer.getInstance().getPlayerManager().getPlayerById(owner);
    }
    
    @JsonProperty("owner")
    private String getOwnerId() {
        return owner;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Item getItem() {
        return item;
    }
    
    public void setProperty(String key, Object value) {
        metadata.put(key, value);
    }
    
    public void removeProperty(String key) {
        metadata.remove(key);
    }
    
    public boolean hasProperty(String key) {
        return metadata.containsKey(key);
    }
    
    public Object getProperty(String key) {
        return metadata.get(key);
    }
    
    public int getIntProperty(String key) {
        return tryParse(key, Integer::parseInt, 0);
    }
        
    public float getFloatProperty(String key) {
        return tryParse(key, Float::parseFloat, 0.0f);
    }
    
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(String.valueOf(getProperty(key)));
    }
    
    public String getStringProperty(String key) {
        Object value = metadata.get(key);
        return value != null && value instanceof String ? (String)value : null;
    }
    
    /**
     * Generic function for parsing a number from a string.
     */
    private <T> T tryParse(String key, Function<String, T> parseFunction, T def) {
        Object value = metadata.get(key);
        
        if(value == null) {
            return def;
        }
        
        T result = def;
        
        try {
            result = parseFunction.apply(String.valueOf(value));
        } catch(NumberFormatException e) {
            // Discard silently
        }
        
        return result;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @JsonIgnore
    public Map<String, Object> getClientMetadata() {
        Map<String, Object> clientMetadata = new HashMap<>(metadata); // Shallow copy
        clientMetadata.put("i", item.getCode());
        
        if(hasOwner()) {
            clientMetadata.put("p", owner);
        }
        
        return clientMetadata;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
