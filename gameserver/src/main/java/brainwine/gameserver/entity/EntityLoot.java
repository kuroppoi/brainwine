package brainwine.gameserver.entity;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import brainwine.gameserver.item.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityLoot {
    
    private final Item item;
    private final int quantity;
    private final double frequency;
    
    @ConstructorProperties({"item", "quantity", "frequency"})
    public EntityLoot(Item item, int quantity, double frequency) {
        this.item = item;
        this.quantity = quantity < 1 ? 1 : quantity;
        this.frequency = frequency < 1 ? 1 : frequency;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getFrequency() {
        return frequency;
    }
}
