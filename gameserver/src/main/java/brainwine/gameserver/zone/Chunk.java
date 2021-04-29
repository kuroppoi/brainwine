package brainwine.gameserver.zone;

/**
 * Simple & convenient class to store block data.
 * Outside of allowing zones to be chopped up unto chunks, it doesn't
 * really have any other purpose.
 */
public class Chunk {
    
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Block[] blocks;
    private boolean modified;
    
    public Chunk(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.blocks = new Block[width * height];
        
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block();
        }
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setBlock(int x, int y, Block block) {
        setBlock(getBlockIndex(x % width, y % height), block);
    }
    
    public void setBlock(int index, Block block) {
        if(isIndexInBounds(index)) {
            blocks[index] = block;
        }
    }
    
    public Block getBlock(int x, int y) {
        return getBlock(getBlockIndex(x % width, y % height));
    }
    
    public Block getBlock(int index) {
        if(!isIndexInBounds(index)) {
            return null;
        }
        
        return blocks[index];
    }
    
    private int getBlockIndex(int x, int y) {
        return y * width + x;
    }
    
    private boolean isIndexInBounds(int index) {
        return index >= 0 && index < blocks.length;
    }
    
    public Block[] getBlocks() {
        return blocks;
    }
}
