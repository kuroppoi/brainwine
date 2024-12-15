package brainwine.gameserver.shop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class ProductImage {
    
    private final Layer[] layers;
    
    @JsonCreator
    public ProductImage(Layer[] layers) {
        this.layers = layers;
    }
    
    @JsonCreator
    public ProductImage(String sprite) {
        layers = new Layer[1];
        layers[0] = new Layer(sprite, null);
    }
    
    public boolean isLayered() {
        return layers.length > 1;
    }
    
    public String getBaseSprite() {
        return layers.length == 0 ? "inventory/air" : layers[0].getSprite();
    }
    
    @JsonValue
    private Layer[] getJsonValue() {
        return layers;
    }
    
    private static class Layer {
        
        private final String sprite;
        private final String color;
        
        @JsonCreator
        public Layer(
                @JsonProperty(value = "sprite", required = true) String sprite,
                @JsonProperty(value = "color") String color) {
            this.sprite = sprite;
            this.color = color;
        }
        
        @JsonGetter("frame")
        public String getSprite() {
            return sprite;
        }
        
        @SuppressWarnings("unused")
        public String getColor() {
            return color;
        }
    }
}
