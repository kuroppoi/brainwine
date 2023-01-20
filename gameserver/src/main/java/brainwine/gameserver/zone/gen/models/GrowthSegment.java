package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.Vector2i;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GrowthSegment {
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("offset")
    private Vector2i offset = new Vector2i(0, 0);
    
    @JsonProperty("frequency")
    private double frequency = 1;
    
    @JsonCreator
    private GrowthSegment(@JsonProperty(value = "item", required = true) Item item) {
        this.item = item;
    }
    
    public Item getItem() {
        return item;
    }
    
    public Vector2i getOffset() {
        return offset;
    }
    
    public double getFrequency() {
        return frequency;
    }
}
