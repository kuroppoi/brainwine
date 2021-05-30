package brainwine.gameserver.zone;

import brainwine.gameserver.item.Item;

public class DugBlock {
    
    private final int x;
    private final int y;
    private final Item item;
    private final int mod;
    private final long time;
    
    public DugBlock(int x, int y, Item item, int mod, long time) {
        this.x = x;
        this.y = y;
        this.item = item;
        this.mod = mod;
        this.time = time;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getMod() {
        return mod;
    }
    
    public long getTime() {
        return time;
    }
}
