package brainwine.gameserver.zone;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;

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
    
    public String getOwner() {
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
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @JsonIgnore
    public Map<String, Object> getClientMetadata() {
        Map<String, Object> clientMetadata = new HashMap<>(metadata); // Shallow copy
        clientMetadata.put("i", item.getId());
        
        if(hasOwner()) {
            clientMetadata.put("p", owner);
        }
        
        return clientMetadata;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
