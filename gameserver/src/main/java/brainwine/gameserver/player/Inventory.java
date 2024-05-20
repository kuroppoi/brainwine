package brainwine.gameserver.player;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.server.messages.WardrobeMessage;

@JsonIncludeProperties({"items", "hotbar", "accessories"})
public class Inventory {
    
    @JsonProperty("items")
    private final Map<Item, Integer> items = new HashMap<>();
    
    // TODO clean up, perhaps just merge with inventory somehow.
    private final ItemContainer hotbar = new ItemContainer(10);
    private final ItemContainer accessories = new ItemContainer(20);
    private Player player;  
    
    protected Inventory() {}
    
    protected Inventory(Player player) {
        this.player = player;
    }
    
    @ConstructorProperties({"hotbar", "accessories"})
    private Inventory(Item[] hotbar, Item[] accessories) {
        if(hotbar != null) {
            for(int i = 0; i < hotbar.length; i++) {
                this.hotbar.moveItem(hotbar[i], i);
            }
        }
        
        if(accessories != null) {
            for(int i = 0; i < accessories.length; i++) {
                this.accessories.moveItem(accessories[i], i);
            }
        }
    }
    
    protected void setPlayer(Player player) {
        this.player = player;
    }
    
    public void moveItemToContainer(Item item, ContainerType type, int slot) {
        boolean accessoriesUpdated = false;
        hotbar.removeItem(item);
        
        if(accessories.hasItem(item)) {
            accessories.removeItem(item);
            accessoriesUpdated = true;
        }
        
        switch(type) {
        case INVENTORY:
            break;
        case HOTBAR:
            hotbar.moveItem(item, slot);
            break;
        case ACCESSORIES:
            accessories.moveItem(item, slot);
            accessoriesUpdated = true;
            break;
        }
        
        if(accessoriesUpdated) {
            player.sendMessageToPeers(new EntityChangeMessage(player.getId(), player.getStatusConfig()));
        }
    }
    
    public void addItem(Item item) {
        addItem(item, false);
    }
    
    public void addItem(Item item, boolean sendMessage) {
        addItem(item, 1, sendMessage);
    }
    
    public void addItem(Item item, int quantity) {
        addItem(item, quantity, false);
    }
    
    public void addItem(Item item, int quantity, boolean sendMessage) {
        setItem(item, getQuantity(item) + quantity, sendMessage);
    }
    
    public void removeItem(Item item) {
        removeItem(item, false);
    }
    
    public void removeItem(Item item, boolean sendMessage) {
        removeItem(item, 1, sendMessage);
    }
    
    public void removeItem(Item item, int quantity) {
        removeItem(item, quantity, false);
    }
    
    public void removeItem(Item item, int quantity, boolean sendMessage) {
        setItem(item, getQuantity(item) - quantity, sendMessage);
    }
    
    private void setItem(Item item, int quantity, boolean sendMessage) {
        if(quantity <= 0) {
            items.remove(item);
            hotbar.removeItem(item);
            
            if(accessories.hasItem(item)) {
                accessories.removeItem(item);
                player.sendMessageToPeers(new EntityChangeMessage(player.getId(), player.getStatusConfig()));
            }
        } else {
            items.put(item, quantity);
        }
        
        if(sendMessage) {
            if(item.isClothing() && quantity > 0) {
                player.sendMessage(new WardrobeMessage(item));
            } else if(!item.isClothing()) {
                player.sendMessage(new InventoryMessage(getClientConfig(item)));
            }
        }
    }
    
    public boolean hasItem(Item item) {
        return hasItem(item, 1);
    }
    
    public boolean hasItem(Item item, int quantity) {
        return getQuantity(item) >= quantity;
    }
    
    public int getQuantity(Item item) {
        return items.getOrDefault(item, 0);
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public Item findJetpack() {
        for(Item item : accessories.getItems()) {
            if(item.hasUse(ItemUseType.FLY)) {
                return item;
            }
        }
        
        return Item.AIR;
    }
    
    public ItemContainer getHotbar() {
        return hotbar;
    }
    
    public ItemContainer getAccessories() {
        return accessories;
    }
    
    public List<Item> getWardrobe() {
        return items.keySet().stream().filter(item -> item.isClothing() && hasItem(item)).collect(Collectors.toList());
    }
    
    @JsonValue
    public Map<String, Object> getJsonValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("items", items);
        map.put("hotbar", hotbar.getItems());
        map.put("accessories", accessories.getItems());
        return map;
    }
    
    private void addItemLocation(Item item, List<Object> itemData) {
        int slot = -1;
        
        if((slot = hotbar.getSlot(item)) != -1) {
            itemData.add(ContainerType.HOTBAR.getId());
            itemData.add(slot);
        } else if((slot = accessories.getSlot(item)) != -1) {
            itemData.add(ContainerType.ACCESSORIES.getId());
            itemData.add(slot);
        } else {
            if(!player.isV3()) {
                itemData.add("i");
                itemData.add(-1);
            }
        }
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link InventoryMessage}.
     */
    public Map<String, Object> getClientConfig() {
        Map<String, Object> data = new HashMap<>();
        
        for(Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            
            // Exclude clothing, as that is sent in WardrobeMessage.
            if(item.isClothing()) {
                continue;
            }
            
            int quantity = entry.getValue();
            List<Object> itemData = new ArrayList<>();
            itemData.add(quantity);
            addItemLocation(item, itemData);
            data.put(String.valueOf(item.getCode()), itemData);
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
        
        if(!player.isV3()) {
            addItemLocation(item, itemData);
        }
        
        data.put(String.valueOf(item.getCode()), itemData);
        return data;
    }
}
