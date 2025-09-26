package brainwine.gameserver.shop;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.server.messages.StatMessage;
import brainwine.gameserver.server.models.PlayerStat;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

/**
 * Manages the Crown Store.
 * Since no real money is involved, we can afford to be pretty careless with its implementation.
 */
public class ShopManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, ShopSection> sections = new LinkedHashMap<>();
    private static final Map<String, Product> products = new LinkedHashMap<>();
    
    public static void loadShopData() {
        logger.info(SERVER_MARKER, "Loading shop data ...");
        sections.clear();
        products.clear();
        
        try {
            URL url = ResourceFinder.getResourceUrl("shop.json");
            Map<String, Object> data = JsonHelper.readValue(url, new TypeReference<Map<String, Object>>(){});
            sections.putAll(JsonHelper.readValue(data.getOrDefault("sections", Collections.emptyMap()), new TypeReference<LinkedHashMap<String, ShopSection>>(){}));
            products.putAll(JsonHelper.readValue(data.getOrDefault("products", Collections.emptyMap()), new TypeReference<LinkedHashMap<String, Product>>(){}));
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not load shop data", e);
            return;
        }
        
        try {
            // Create section data
            for(Entry<String, ShopSection> entry : sections.entrySet()) {
                Map<String, Object> data = JsonHelper.readValue(entry.getValue(), new TypeReference<Map<String, Object>>(){});
                data.put("key", entry.getKey());
                data.put("items", data.remove("products"));
                MapHelper.appendList(GameConfiguration.getBaseConfig(), "shop.sections", data);
            }
            
            // Create product data
            for(Entry<String, Product> entry : products.entrySet()) {
                Product product = entry.getValue();
                ProductImage image = product.getImage();
                
                // Skip product if it isn't available
                if(!product.isAvailable()) {
                    continue;
                }
                
                Map<String, Object> data = JsonHelper.readValue(product, new TypeReference<Map<String, Object>>(){});
                data.put("key", entry.getKey());
                
                // Convert inventory data
                if(data.containsKey("items")) {
                    Map<String, Object> inventoryData = (Map<String, Object>)data.remove("items"); // TODO this is kinda shit
                    inventoryData.forEach((item, quantity) -> MapHelper.appendList(data, "inventory", Arrays.asList(item, quantity)));
                }
                
                // Convert image data
                if(image != null && data.containsKey("image")) {                    
                    if(image.isLayered()) {
                        data.put("images", data.remove("image"));
                    }
                    
                    data.put("image", image.getBaseSprite());
                }
                
                MapHelper.appendList(GameConfiguration.getBaseConfig(), "shop.items", data);
            }
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "An error occured while converting shop data", e);
            products.clear(); // Clear products so purchases can't be made
            return;
        }
        
        logger.info(SERVER_MARKER, "Successfully loaded {} product{}", products.size(), products.size() == 1 ? "" : "s");
    }
    
    public static boolean purchaseProduct(Player player, Product product) {
        // Check if item is available
        if(product == null || !product.isAvailable()) {
            player.notify("Oops! There was an error with your purchase.");
            player.sendMessage(new StatMessage(PlayerStat.CROWNS, player.getCrowns()));
            return false;
        }
        
        // Check if player has enough crowns
        if(player.getCrowns() < product.getCost()) {
            player.notify("You do not have enough crowns to buy this.");
            player.sendMessage(new StatMessage(PlayerStat.CROWNS, player.getCrowns()));
            return false;
        }
        
        // Run product-specific validation
        if(!product.validate(player)) {
            player.sendMessage(new StatMessage(PlayerStat.CROWNS, player.getCrowns()));
            return false;
        }
        
        player.setCrowns(player.getCrowns() - product.getCost());
        product.purchase(player);
        return true;
    }
    
    public static boolean purchaseProduct(Player player, String productKey) {
        return purchaseProduct(player, products.get(productKey));
    }
    
    public static Product getProduct(String key) {
        return products.get(key);
    }
}
