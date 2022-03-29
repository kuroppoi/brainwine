package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.models.BlockPosition;

public class GeneratorContext {
    
    private final List<Cave> caves = new ArrayList<>();
    private final Map<BlockPosition, Prefab> prefabs = new HashMap<>();
    private final Zone zone;
    private final Random random;
    private final int seed;
    
    public GeneratorContext(Zone zone, int seed) {
        this.zone = zone;
        this.seed = seed;
        random = new Random(seed);
        
        for(int i = 0; i < zone.getChunkCount(); i++) {
            int x = i % zone.getNumChunksWidth() * zone.getChunkWidth();
            int y = i / zone.getNumChunksWidth() * zone.getChunkHeight();
            zone.putChunk(i, new Chunk(x, y, zone.getChunkWidth(), zone.getChunkHeight()));
        }
    }
    
    public void addCave(Cave cave) {
        caves.add(cave);
    }
    
    public List<Cave> getCaves() {
        return caves;
    }
    
    public void placePrefab(Prefab prefab, int x, int y) {
        if(!willPrefabOverlap(prefab, x, y)) {
            zone.placePrefab(prefab, x, y, random);
            prefabs.put(new BlockPosition(x, y), prefab);
        }
    }
    
    public boolean willPrefabOverlap(Prefab prefab, int x, int y) {
        for(Entry<BlockPosition, Prefab> entry : prefabs.entrySet()) {
            BlockPosition position = entry.getKey();
            Prefab other = entry.getValue();
            int x2 = position.getX();
            int y2 = position.getY();
            
            if(x + prefab.getWidth() >= x2 && x <= x2 + other.getWidth() && y + prefab.getHeight() >= y2 && y <= y2 + other.getHeight()) {
                return true;
            }
        }
        
        return false;
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
        return random.nextInt(Math.max(bound, 1));
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
