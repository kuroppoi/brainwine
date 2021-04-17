package brainwine.gameserver.zone;

public enum SizePreset {
    
    MINI(1000, 400),
    NORMAL(2000, 800),
    XL(4000, 1600);
    
    private final int width;
    private final int height;
    
    private SizePreset(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
