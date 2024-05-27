package brainwine.gameserver.shop;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import brainwine.gameserver.player.Player;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(name = "item", value = ItemProduct.class),
    @Type(name = "zone", value = ZoneProduct.class),
})
public abstract class Product {
    
    protected final String name;
    protected final int cost;
    protected boolean available = true;
    protected String category = "all";
    protected String description = "No description is available for this product.";
    protected String image = "inventory/air";
    
    public Product(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
    
    public abstract void purchase(Player player);
    
    public String getName() {
        return name;
    }
    
    public int getCost() {
        return cost;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImage() {
        return image;
    }
}
