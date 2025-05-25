package brainwine.gameserver.zone;

/**
 * Used by {@link SteamManager} to keep track of things.
 */
public class SteamIteration {
    
    private int x;
    private int y;
    private byte direction;
    private short depth;
    
    public SteamIteration(int x, int y, int direction, int depth) {
        this.x = x;
        this.y = y;
        this.direction = (byte)direction;
        this.depth = (short)depth;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public byte getDirection() {
        return direction;
    }
    
    public short getDepth() {
        return depth;
    }
}
