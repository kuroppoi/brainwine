package brainwine.gameserver.shop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopSection {
    
    private final String name;
    private final String icon;
    private final String[] products;
    
    @JsonCreator
    public ShopSection(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "icon", required = true) String icon,
            @JsonProperty(value = "products", required = true) String... products) {
        this.name = name;
        this.icon = icon;
        this.products = products;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String[] getProducts() {
        return products;
    }
}
