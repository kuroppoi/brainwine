package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CraftingIngredient {
    
    private String item;
    private int quantity;
    
    public CraftingIngredient(String item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
    
    @JsonCreator
    public CraftingIngredient(String item) {
        this(item, 1);
    }
    
    @JsonCreator
    private CraftingIngredient(Object[] data) {
        this((String)data[0], (int)data[1]);
    }
    
    public Item getItem() {
        return ItemRegistry.getItem(item);
    }
    
    public int getQuantity() {
        return quantity;
    }
}
