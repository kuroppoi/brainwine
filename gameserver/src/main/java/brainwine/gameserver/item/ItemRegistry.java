package brainwine.gameserver.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, Item> items = new HashMap<>();
    private static final Map<String, Item> itemsByName = new HashMap<>();
    
    static {
        //registerItem(Item.AIR);
    }
    
    public static boolean registerItem(Item item) {
        int id = item.getId();
        String name = item.getName();
        
        if(items.containsKey(id)) {
            logger.warn("Duplicate item id {} for item {}", id, name);
            return false;
        }
        
        if(itemsByName.containsKey(name)) {
            logger.warn("Duplicate item name {} for id {}", name, id);
            return false;
        }
        
        items.put(id, item);
        itemsByName.put(name, item);
        return true;
    }
    
    public static Item getItem(int id) {
        return items.getOrDefault(id, Item.AIR);
    }
    
    public static Item getItem(String name) {
        return itemsByName.getOrDefault(name, Item.AIR);
    }
    
    public static Collection<Item> getItems(){
        return items.values();
    }
}
