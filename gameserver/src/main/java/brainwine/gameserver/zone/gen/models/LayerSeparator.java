package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerSeparator {
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("min_thickness")
    private int minThickness = 3;
    
    @JsonProperty("max_thickness")
    private int maxThickness = 6;
    
    @JsonProperty("min_amplitude")
    private double minAmplitude = 20;
    
    @JsonProperty("max_amplitude")
    private double maxAimplitude = 20;
    
    @JsonCreator
    private LayerSeparator(@JsonProperty(value = "item", required = true) Item item) {
        this.item = item;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getMinThickness() {
        return minThickness;
    }
    
    public int getMaxThickness() {
        return maxThickness;
    }
    
    public double getMinAmplitude() {
        return minAmplitude;
    }
    
    public double getMaxAmplitude() {
        return maxAimplitude;
    }
}
