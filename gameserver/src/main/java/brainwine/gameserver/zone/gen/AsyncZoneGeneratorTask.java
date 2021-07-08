package brainwine.gameserver.zone.gen;

import java.util.function.Consumer;

import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

public class AsyncZoneGeneratorTask {
    
    private final Biome biome;
    private final int width;
    private final int height;
    private final int seed;
    private final Consumer<Zone> callback;
    
    public AsyncZoneGeneratorTask(Biome biome, int width, int height, int seed, Consumer<Zone> callback) {
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
    
    public Consumer<Zone> getCallback() {
        return callback;
    }
}
