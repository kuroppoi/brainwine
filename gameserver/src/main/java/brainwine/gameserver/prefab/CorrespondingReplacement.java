package brainwine.gameserver.prefab;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CorrespondingReplacement {
    
    @JsonProperty("key")
    private Item key;
    
    @JsonProperty("values")
    private Map<Item, Item> values = new HashMap<>();
    
    @JsonCreator
    private CorrespondingReplacement() {}
    
    public Item getKey() {
        return key;
    }
    
    public Map<Item, Item> getValues() {
        return values;
    }
}
