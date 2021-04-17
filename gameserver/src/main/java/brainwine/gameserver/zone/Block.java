package brainwine.gameserver.zone;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;

/**
 * TODO store block owners.
 */
public class Block {
    
    private Item baseItem;
    private Item backItem;
    private int backMod;
    private Item frontItem;
    private int frontMod;
    private Item liquidItem;
    private int liquidMod;
    
    public Block() {
        this(0, 0, 0, 0, 0, 0, 0);
    }
    
    public Block(int baseItem, int backItem, int backMod, int frontItem, int frontMod, int liquidItem, int liquidMod) {
        this(ItemRegistry.getItem(baseItem), ItemRegistry.getItem(backItem), backMod, ItemRegistry.getItem(frontItem), frontMod, ItemRegistry.getItem(liquidItem), liquidMod);
    }
    
    public Block(Item baseItem, Item backItem, int backMod, Item frontItem, int frontMod, Item liquidItem, int liquidMod) {
        this.baseItem = baseItem;
        this.backItem = backItem;
        this.backMod = backMod;
        this.frontItem = frontItem;
        this.frontMod = frontMod;
        this.liquidItem = liquidItem;
        this.liquidMod = liquidMod;
    }
    
    public void updateLayer(Layer layer, int item) {
        updateLayer(layer, item, 0);
    }
    
    public void updateLayer(Layer layer, int item, int mod) {
        updateLayer(layer, ItemRegistry.getItem(item), mod);
    }
    
    public void updateLayer(Layer layer, Item item) {
        updateLayer(layer, item, 0);
    }
    
    public void updateLayer(Layer layer, Item item, int mod) {
        switch(layer) {
        case BASE:
            baseItem = item;
            break;
        case BACK:
            backItem = item;
            backMod = mod;
            break;
        case FRONT:
            frontItem = item;
            frontMod = mod;
            break;
        case LIQUID:
            liquidItem = item;
            liquidMod = mod;
            break;
        default:
            break;
        }
    }
    
    public void setItem(Layer layer, int item) {
        setItem(layer, ItemRegistry.getItem(item));
    }
    
    public void setItem(Layer layer, Item item) {
        switch(layer) {
        case BASE:
            baseItem = item;
            break;
        case BACK:
            backItem = item;
            break;
        case FRONT:
            frontItem = item;
            break;
        case LIQUID:
            liquidItem = item;
            break;
        default:
            break;
        }
    }
    
    public Item getItem(Layer layer) {
        switch(layer) {
        case BASE:
            return baseItem;
        case BACK:
            return backItem;
        case FRONT:
            return frontItem;
        case LIQUID:
            return liquidItem;
        default:
            return Item.AIR;
        }
    }
    
    public void setMod(Layer layer, int mod) {
        switch(layer) {
        case BACK:
            backMod = mod;
            break;
        case FRONT:
            frontMod = mod;
            break;
        case LIQUID:
            liquidMod = mod;
            break;
        default:
            break;
        }
    }
    
    public int getMod(Layer layer) {
        switch(layer) {
        case BACK:
            return backMod;
        case FRONT:
            return frontMod;
        case LIQUID:
            return liquidMod;
        default:
            return 0;
        }
    }
    
    public Item getBaseItem() {
        return baseItem;
    }
    
    public Item getBackItem() {
        return backItem;
    }
    
    public int getBackMod() {
        return backMod;
    }
    
    public Item getFrontItem() {
        return frontItem;
    }
    
    public int getFrontMod() {
        return frontMod;
    }
    
    public Item getLiquidItem() {
        return liquidItem;
    }
    
    public int getLiquidMod() {
        return liquidMod;
    }
}
