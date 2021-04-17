package brainwine.gameserver.zone.gen;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.Zone;

public class GeneratorContext {
    
    private static final String[] FIRST_NAMES = {
        "Malvern",
        "Tralee",
        "Horncastle",
        "Old",
        "Westwood",
        "Citta",
        "Tadley",
        "Mossley",
        "West",
        "East",
        "North",
        "South",
        "Wadpen",
        "Githam",
        "Soatnust",
        "Highworth",
        "Creakynip",
        "Upper",
        "Lower",
        "Cannock",
        "Dovercourt",
        "Limerick",
        "Pickering",
        "Glumshed",
        "Crusthack"
    };
    
    private static final String[] LAST_NAMES = {
        "Falls",
        "Alloa",
        "Glen",
        "Way",
        "Dolente",
        "Peak",
        "Heights",
        "Creek",
        "Banffshire",
        "Chagford",
        "Gorge",
        "Valley",
        "Catacombs",
        "Depths",
        "Mines",
        "Crickbridge",
        "Guildbost",
        "Pits",
        "Vaults",
        "Ruins",
        "Dell"
    };
    
    private final UUID zoneId;
    private final Biome biome;
    private final int width;
    private final int height;
    private final int[] surface;
    private final Block[] blocks;
    private final Random random;
    private final int seed;
    
    public GeneratorContext(Biome biome, int width, int height) {
        zoneId = UUID.randomUUID();
        this.seed = (int)(zoneId.getMostSignificantBits() >> 32 & 0x00FFFFFF);
        this.biome = biome;
        this.width = width;
        this.height = height;
        this.surface = new int[width];
        this.blocks = new Block[width * height];
        this.random = new Random(seed);
        Arrays.fill(surface, height);
        
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block();
        }
    }
    
    public void updateBlock(int x, int y, Layer layer, int item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod) {
        updateBlock(x, y, layer, ItemRegistry.getItem(item), mod);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        if(layer == Layer.BASE) {
            if(y < surface[x]) {
                surface[x] = y;
            }
        }
        
        getBlock(x, y).updateLayer(layer, item, mod);
    }
    
    public int getBlockIndex(int x, int y) {
        return y * width + x;
    }
    
    public Block getBlock(int x, int y) {
        if(!areCoordinatesInBounds(x, y)) {
            return null;
        }
        
        return blocks[getBlockIndex(x, y)];
    }
    
    public int nextInt() {
        return random.nextInt();
    }
    
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
    
    public float nextFloat() {
        return random.nextFloat();
    }
    
    public double nextDouble() {
        return random.nextDouble();
    }
    
    public int getSeed() {
        return seed;
    }
    
    public boolean areCoordinatesInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public Biome getBiome() {
        return biome;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean isUnderground(int x, int y) {
        return y >= surface[x];
    }
    
    public int[] getSurface() {
        return surface;
    }
    
    public Zone constructZone() {
        // We don't wanna rely on the seed for names.
        String firstName = FIRST_NAMES[(int)(Math.random() * FIRST_NAMES.length)];
        String lastName = LAST_NAMES[(int)(Math.random() * LAST_NAMES.length)];
        String name = firstName + " " + lastName;
        Zone zone = new Zone(zoneId.toString(), name, biome, width, height);
        
        // Create chunks
        for(int i = 0; i < zone.getNumChunksWidth() * zone.getNumChunksHeight(); i++) {
            int x = i % zone.getNumChunksWidth() * zone.getChunkWidth();
            int y = i / zone.getNumChunksWidth() * zone.getChunkHeight();
            zone.putChunk(i, new Chunk(x, y, zone.getChunkWidth(), zone.getChunkHeight()));
        }
        
        // Place blocks
        for(int i = 0; i < blocks.length; i++) {
            Block block = blocks[i];
            int x = i % zone.getWidth();
            int y = i / zone.getWidth();
            zone.updateBlock(x, y, Layer.BASE, block.getBaseItem(), 0);
            zone.updateBlock(x, y, Layer.BACK, block.getBackItem(), block.getBackMod());
            zone.updateBlock(x, y, Layer.FRONT, block.getFrontItem(), block.getFrontMod());
            zone.updateBlock(x, y, Layer.LIQUID, block.getLiquidItem(), block.getLiquidMod());
        }
        
        zone.setSurface(surface);
        
        return zone;
    }
}
