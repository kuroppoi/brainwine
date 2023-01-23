package brainwine.gameserver.zone.gen.models;

public class ModTileBlock {
    
    private final String item;
    private final int width;
    private final int height;
    
    public ModTileBlock(String item, int width, int height) {
        this.item = item;
        this.width = width;
        this.height = height;
    }
    
    public String getItem() {
        return item;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
