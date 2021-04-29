package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.gen.models.Cave;

public class GeneratorContext {
    
    private final List<Cave> caves = new ArrayList<>();
    private final Zone zone;
    private final Random random;
    private final int seed;
    
    public GeneratorContext(Zone zone, int seed) {
        this.zone = zone;
        this.seed = seed;
        random = new Random(seed);
    }
    
    public void addCave(Cave cave) {
        caves.add(cave);
    }
    
    public List<Cave> getCaves() {
        return caves;
    }
    
    public void updateBlock(int x, int y, Layer layer, int item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod) {
        if(inBounds(x, y)) {
            zone.updateBlock(x, y, layer, item, mod);
        }
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item) {
        updateBlock(x, y, layer, item, 0);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod) {
        if(inBounds(x, y)) {
            zone.updateBlock(x, y, layer, item, mod);
        }
    }
    
    public boolean inBounds(int x, int y) {
        return zone.areCoordinatesInBounds(x, y);
    }
    
    public boolean isUnderground(int x, int y) { 
        return zone.areCoordinatesInBounds(x, y) && y >= zone.getSurface()[x];
    }
    
    public boolean isEarth(int x, int y) {
        if(!zone.areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        int item = zone.getBlock(x, y).getFrontItem().getId();
        return item == 512;
    }
    
    public int getWidth() {
        return zone.getWidth();
    }
    
    public int getHeight() {
        return zone.getHeight();
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public int nextInt() {
        return random.nextInt();
    }
    
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
    
    public double nextDouble() {
        return random.nextDouble();
    }
    
    public Random getRandom() {
        return random;
    }
    
    public int getSeed() {
        return seed;
    }
}
