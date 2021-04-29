package brainwine.gameserver.zone.gen.models;

public class ModTileBlock {
    
    private int item;
    private int width;
    private int height;
    
    public ModTileBlock(int item, int width, int height) {
        this.item = item;
        this.width = width;
        this.height = height;
    }
    
    public int getItem() {
        return item;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
