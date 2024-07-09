package brainwine.gameserver.zone;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;

public class Block {
    
    private Item baseItem;
    private Item backItem;
    private byte backMod;
    private Item frontItem;
    private byte frontMod;
    private Item liquidItem;
    private byte liquidMod;
    private short ownerHash;
    
    public Block() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }
    
    public Block(int base, int back, int front) {
        this(base & 15, back & 65535, back >> 16 & 31, front & 65535, front >> 16 & 31, base >> 8 & 255, base >> 16 & 31, front >> 21 & 2047);
    }
    
    public Block(int baseItem, int backItem, int backMod, int frontItem, int frontMod, int liquidItem, int liquidMod, int ownerHash) {
        this(ItemRegistry.getItem(baseItem), ItemRegistry.getItem(backItem), backMod, 
                ItemRegistry.getItem(frontItem), frontMod, ItemRegistry.getItem(liquidItem), liquidMod, ownerHash);
    }
    
    public Block(Item baseItem, Item backItem, int backMod, Item frontItem, int frontMod, Item liquidItem, int liquidMod, int ownerHash) {
        updateLayer(Layer.BASE, baseItem, 0, ownerHash);
        updateLayer(Layer.BACK, backItem, backMod, ownerHash);
        updateLayer(Layer.FRONT, frontItem, frontMod, ownerHash);
        updateLayer(Layer.LIQUID, liquidItem, liquidMod, ownerHash);
    }

    public boolean isSolid() {
        return (frontItem.isDoor() && frontMod % 2 == 0) || (!frontItem.isDoor() && frontItem.isSolid());
    }
    
    public void updateLayer(Layer layer, int item) {
        updateLayer(layer, item, 0);
    }
    
    public void updateLayer(Layer layer, int item, int mod) {
        updateLayer(layer, item, mod, 0);
    }
    
    public void updateLayer(Layer layer, int item, int mod, int owner) {
        updateLayer(layer, ItemRegistry.getItem(item), mod, owner);
    }
    
    public void updateLayer(Layer layer, Item item) {
        updateLayer(layer, item, 0);
    }
    
    public void updateLayer(Layer layer, Item item, int mod) {
        updateLayer(layer, item, mod, 0);
    }
    
    public void updateLayer(Layer layer, Item item, int mod, int owner) {
        switch(layer) {
        case BASE:
            baseItem = item;
            break;
        case BACK:
            backItem = item;
            backMod = (byte)(mod & 31);
            break;
        case FRONT:
            frontItem = item;
            frontMod = (byte)(mod & 31);
            ownerHash = (short)(item.isAir() ? 0 : owner & 2047);
            break;
        case LIQUID:
            liquidItem = item;
            liquidMod = (byte)(mod & 31);
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
            backMod = (byte)(mod & 31);
            break;
        case FRONT:
            frontMod = (byte)(mod & 31);
            break;
        case LIQUID:
            liquidMod = (byte)(mod & 31);
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
    
    public int getBase() {
        return baseItem.getCode() | (((liquidItem.getCode() & 255) << 8) | ((liquidMod) & 31) << 16);
    }
    
    public Item getBackItem() {
        return backItem;
    }
    
    public int getBackMod() {
        return backMod;
    }
    
    public int getBack() {
        return backItem.getCode() | ((backMod & 31) << 16);
    }
    
    public Item getFrontItem() {
        return frontItem;
    }
    
    public int getFrontMod() {
        return frontMod;
    }
    
    public int getFront() {
        return frontItem.getCode() | ((ownerHash & 2047) << 21) | ((frontMod & 31) << 16);
    }
    
    public Item getLiquidItem() {
        return liquidItem;
    }
    
    public int getLiquidMod() {
        return liquidMod;
    }
    
    public boolean isNatural() {
        return ownerHash == 0;
    }
    
    public int getOwnerHash() {
        return ownerHash;
    }
}
