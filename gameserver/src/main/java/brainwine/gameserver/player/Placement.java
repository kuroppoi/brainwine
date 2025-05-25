package brainwine.gameserver.player;

import brainwine.gameserver.item.Item;

/**
 * Simple model for keeping track of the last placed item of a player.
 * Mainly used for linking switches to doors, trapdoors etc.
 */
public class Placement {
    
    private final int x;
    private final int y;
    private final Item item;
    
    public Placement(int x, int y, Item item) {
        this.x = x;
        this.y = y;
        this.item = item;
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
}
