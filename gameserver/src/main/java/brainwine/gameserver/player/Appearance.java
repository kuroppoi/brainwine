package brainwine.gameserver.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.util.MapHelper;

/**
 * Utility class for player appearance related stuff.
 * Ghosts also have a random appearance, which is why it's here instead of in the player class.
 */
public class Appearance {
    
    public static Map<String, Object> getRandomAppearance() {
        return getRandomAppearance(null);
    }
    
    public static Map<String, Object> getRandomAppearance(Player player) {
        Map<String, Object> appearance = new HashMap<>();
        
        for(AppearanceSlot slot : AppearanceSlot.values()) {
            // Skip if slot cannot be changed by players
            if(!slot.isChangeable()) {
                continue;
            }
            
            String category = slot.getCategory();
            
            // Color handling
            if(slot.isColor()) {
                List<String> colors = getAvailableColors(slot, player);
                
                // Change appearance to random color
                if(!colors.isEmpty()) {
                    appearance.put(slot.getId(), colors.get((int)(Math.random() * colors.size())));
                }
                
                continue;
            }
            
            // Fetch list of items in this slot's category that the player owns
            List<Item> items = ItemRegistry.getItemsByCategory(category).stream()
                    .filter(item -> item.isBase() || (player != null && player.getInventory().hasItem(item)))
                    .collect(Collectors.toList());
            
            // Change appearance to random clothing item
            if(!items.isEmpty()) {
                appearance.put(slot.getId(), items.get((int)(Math.random() * items.size())).getCode());
            }
        }
        
        return appearance;
    }
    
    public static List<String> getAvailableColors(AppearanceSlot slot) {
        return getAvailableColors(slot, null);
    }
    
    public static List<String> getAvailableColors(AppearanceSlot slot, Player player) {
        List<String> colors = new ArrayList<>();
        
        // Return empty list if slot is not valid
        if(!slot.isColor()) {
            return colors;
        }
        
        Map<String, Object> wardrobe = MapHelper.getMap(GameConfiguration.getBaseConfig(), "wardrobe", Collections.emptyMap());
        String category = slot.getCategory();
        
        // Add base colors
        colors.addAll(MapHelper.getList(wardrobe, category, Collections.emptyList()));
        
        // Add bonus colors
        if(player != null && player.getInventory().hasItem(ItemRegistry.getItem("accessories/makeup"))) {
            colors.addAll(MapHelper.getList(wardrobe, String.format("%s-bonus", category), Collections.emptyList()));
        }
        
        return colors;
    }
}
