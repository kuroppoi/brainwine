package brainwine.gameserver.item;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Item> items = new HashMap<>();
    private static final Map<Integer, Item> itemsByCode = new HashMap<>();
    private static final Map<String, List<Item>> itemsByCategory = new HashMap<>();
    
    // TODO maybe just move the registry stuff here
    public static void clear() {
        items.clear();
        itemsByCode.clear();
    }
    
    public static boolean registerItem(Item item) {
        String id = item.getId();
        int code = item.getCode();
        
        if(items.containsKey(id)) {
            logger.warn(SERVER_MARKER, "Duplicate item id {} for code {}", id, code);
            return false;
        }
        
        if(itemsByCode.containsKey(code)) {
            logger.warn(SERVER_MARKER, "Duplicate item code {} for id {}", code, id);
            return false;
        }
        
        String category = item.getCategory();
        List<Item> categorizedItems = itemsByCategory.get(category);
        
        if(categorizedItems == null) {
            categorizedItems = new ArrayList<>();
            itemsByCategory.put(category, categorizedItems);
        }
        
        categorizedItems.add(item);
        items.put(id, item);
        itemsByCode.put(code, item);
        return true;
    }
    
    public static Item getItem(String id) {
        return items.getOrDefault(id, Item.AIR);
    }
    
    public static Item getItem(int code) {
        return itemsByCode.getOrDefault(code, Item.AIR);
    }
    
    public static Collection<Item> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }
    
    public static List<Item> getItemsByCategory(String category) {
        return Collections.unmodifiableList(itemsByCategory.getOrDefault(category, Collections.emptyList()));
    }
}
