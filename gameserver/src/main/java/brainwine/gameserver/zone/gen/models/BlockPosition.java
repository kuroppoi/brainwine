package brainwine.gameserver.zone.gen.models;

public class BlockPosition {
    
    private final int x;
    private final int y;
    
    public BlockPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}
