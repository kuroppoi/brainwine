package brainwine.gameserver.zone.gen;

import brainwine.gameserver.zone.Biome;

public class AsyncZoneGeneratorTask {
    
    private final Biome biome;
    private final int width;
    private final int height;
    private final int seed;
    private final AsyncZoneGeneratedHandler callback;
    
    public AsyncZoneGeneratorTask(Biome biome, int width, int height, int seed, AsyncZoneGeneratedHandler callback) {
        this.biome = biome;
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.callback = callback;
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
    
    public int getSeed() {
        return seed;
    }
    
    public AsyncZoneGeneratedHandler getCallback() {
        return callback;
    }
}
