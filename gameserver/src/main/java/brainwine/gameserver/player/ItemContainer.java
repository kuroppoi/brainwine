package brainwine.gameserver.player;

import java.util.Arrays;

import brainwine.gameserver.item.Item;

public class ItemContainer {
    
    private final Item[] items;
    
    public ItemContainer(int size) {
        items = new Item[size];
        Arrays.fill(items, Item.AIR);
    }
    
    public void moveItem(Item item, int slot) {
        for(int i = 0; i < items.length; i++) {
            if(i == slot) {
                items[i] = item;
            } else if(items[i] == item) {
                items[i] = Item.AIR;
            }
        }
    }
    
    public void removeItem(Item item) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == item) {
                items[i] = Item.AIR;
                return;
            }
        }
    }
    
    public boolean hasItem(Item item) {
        for(Item itemInContainer : items) {
            if(itemInContainer == item) {
                return true;
            }
        }
        
        return false;
    }
    
    public Item getItem(int slot) {
        if(slot < 0 || slot >= items.length) {
            return Item.AIR;
        }
        
        return items[slot];
    }
    
    public int getSlot(Item item) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == item) {
                return i;
            }
        }
        
        return -1;
    }
    
    public Item[] getItems() {
        return items;
    }
}
