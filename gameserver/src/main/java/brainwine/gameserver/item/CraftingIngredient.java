package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CraftingIngredient {
    
    private final LazyItemGetter item;
    private final int quantity;
    
    private CraftingIngredient(LazyItemGetter item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
    
    @JsonCreator
    private CraftingIngredient(String item) {
        this(new LazyItemGetter(item), 1);
    }
    
    @JsonCreator
    private CraftingIngredient(Object[] data) {
        this(new LazyItemGetter((String)data[0]), (int)data[1]);
    }
    
    public Item getItem() {
        return item.get();
    }
    
    public int getQuantity() {
        return quantity;
    }
}
