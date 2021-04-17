package brainwine.gameserver.entity.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.server.messages.InventoryMessage;

public class Inventory {
    
    private final Map<Item, Integer> items = new HashMap<>();
    private final ItemContainer hotbar = new ItemContainer(10);
    
    public Inventory() {
        for(Item item : ItemRegistry.getItems()) {
            items.put(item, 9999);
        }
    }
    
    public void moveItemToContainer(Item item, ContainerType type, int slot) {
        switch(type) {
        case HOTBAR:
            moveItemToContainer(item, hotbar, slot);
            break;
        case ACCESSORIES:
            break;
        }
    }
    
    public void moveItemToContainer(Item item, ItemContainer container, int slot) {
        container.moveItem(item, slot);
    }
    
    public void addItem(Item item) {
        addItem(item, 1);
    }
    
    public void addItem(Item item, int quantity) {
        setItem(item, getQuantity(item) + quantity);
    }
    
    public void removeItem(Item item) {
        removeItem(item, 1);
    }
    
    public void removeItem(Item item, int quantity) {
        setItem(item, getQuantity(item) - quantity);
    }
    
    public void setItem(Item item, int quantity) {
        items.put(item, quantity);
    }
    
    public boolean hasItem(Item item) {
        return getQuantity(item) > 0;
    }
    
    public int getQuantity(Item item) {
        return items.getOrDefault(item, 0);
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link InventoryMessage}.
     */
    public Map<String, Object> getClientConfig() {
        Map<String, Object> data = new HashMap<>();
        
        for(Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            int quantity = entry.getValue();
            List<Object> itemData = new ArrayList<>();
            itemData.add(quantity);
            int slot = -1;
            
            if((slot = hotbar.getSlot(item)) != -1) {
                itemData.add(ContainerType.HOTBAR.id);
                itemData.add(slot);
            }
            
            data.put(String.valueOf(item.getId()), itemData);
        }
        
        return data;
    }
    
    /**
     * @return A {@link Map} containing information about a specific item.
     * @param item
     */
    public Map<String, Object> getClientConfig(Item item) {
        Map<String, Object> data = new HashMap<>();
        List<Object> itemData = new ArrayList<>();
        itemData.add(getQuantity(item));
        data.put(String.valueOf(item.getId()), itemData);
        return data;
    }
}
