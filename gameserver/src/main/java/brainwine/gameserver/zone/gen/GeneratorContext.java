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
import brainwine.gameserver.util.SimplexNoise;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.surface.SurfaceRegion;

public class GeneratorContext {
    
    private final List<SurfaceRegion> surfaceRegions = new ArrayList<>();
    private final List<Cave> caves = new ArrayList<>();
    private final Map<Vector2i, Vector2i> prefabRegions = new HashMap<>();
    private final Zone zone;
    private final int seed;
    private final Random random;
    
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
    
    public void addSurfaceRegion(SurfaceRegion region) {
        surfaceRegions.add(region);
    }
    
    public List<SurfaceRegion> getSurfaceRegions() {
        return surfaceRegions;
    }
    
    public void addCave(Cave cave) {
        caves.add(cave);
    }
    
    public List<Cave> getCaves() {
        return caves;
    }
    
    public boolean placePrefab(Prefab prefab, int x, int y) {
        x = Math.max(1, Math.min(x, getWidth() - prefab.getWidth() - 1));
        y = Math.max(1, Math.min(y, getHeight() - prefab.getHeight() - 3));
        
        if(!willPrefabOverlap(prefab, x, y)) {
            zone.placePrefab(prefab, x, y, random, seed);
            prefabRegions.put(new Vector2i(x, y), new Vector2i(prefab.getWidth(), prefab.getHeight()));
            return true;
        }
        
        return false;
    }
    
    public boolean placePrefabSurface(Prefab prefab, int x) {
        x = Math.max(1, Math.min(x, getWidth() - prefab.getWidth() - 1));
        int[] surface = zone.getSurface();
        int width = prefab.getWidth();
        int height = prefab.getHeight();
        
        // Find highest and lowest points
        int highestPoint = surface[x];
        int lowestPoint = surface[x];
        
        for(int x1 = x; x1 < x + width; x1++) {
            if(x1 >= surface.length) {
                return false; // Might as well return here as it won't place the prefab anyway.
            }
            
            int currentSurface = surface[x1];
            
            if(currentSurface < highestPoint) {
                highestPoint = currentSurface;
            } else if(currentSurface > lowestPoint) {
                lowestPoint = currentSurface;
            }
        }
        
        // Compromise around the middle!
        int y = highestPoint - height + (lowestPoint - highestPoint) / 2;
        y = Math.max(1, Math.min(y, getHeight() - prefab.getHeight() - 3));
        
        // Place the prefab and generate scaffolding if successful
        if(placePrefab(prefab, x, y)) {
            placeScaffolding(prefab, x, y + height);
            return true;
        }
        
        return false;
    }
    
    private void placeScaffolding(Prefab prefab, int x, int y) {
        boolean ruin = prefab.isRuin();
        int width = prefab.getWidth();
        
        for(int i = x; i < x + width; i++) {
            // Don't place scaffolding unless there is a block right above it
            if(zone.getBlock(i, y - 1).getFrontItem().isAir()) {
                continue;
            }
            
            int j = y;
            Block block = null;
            
            // Place scaffolding all the way down until it hits solid ground
            while((block = zone.getBlock(i, j)) != null && block.getBaseItem().isAir() && !block.getFrontItem().isWhole()) {
                if(!ruin || SimplexNoise.noise2(seed, i / 8.0, j / 8.0, 2) <= 0.4) {
                    updateBlock(i, j, Layer.BACK, 258);
                }
                
                j++;
            }
        }
    }
    
    public boolean willPrefabOverlap(Prefab prefab, int x, int y) {
        for(Entry<Vector2i, Vector2i> entry : prefabRegions.entrySet()) {
            Vector2i position = entry.getKey();
            Vector2i size = entry.getValue();
            int x2 = position.getX();
            int y2 = position.getY();
            
            if(x + prefab.getWidth() >= x2 && x <= x2 + size.getX() && y + prefab.getHeight() >= y2 && y <= y2 + size.getY()) {
                return true;
            }
        }
        
        return false;
    }
    
    public void updateBlock(int x, int y, Layer layer, int item) {
        zone.updateBlock(x, y, layer, item);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod) {
        zone.updateBlock(x, y, layer, item, mod);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod, Map<String, Object> metadata) {
        zone.updateBlock(x, y, layer, item, mod, null, metadata);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item) {
        zone.updateBlock(x, y, layer, item);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod) {
        zone.updateBlock(x, y, layer, item, mod);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod, Map<String, Object> metadata) {
        zone.updateBlock(x, y, layer, item, mod, null, metadata);
    }
    
    public boolean inBounds(int x, int y) {
        return zone.areCoordinatesInBounds(x, y);
    }
    
    public boolean isUnderground(int x, int y) { 
        return zone.areCoordinatesInBounds(x, y) && y >= zone.getSurface()[x];
    }
    
    public boolean isOccupied(int x, int y, Layer layer) {
        return zone.areCoordinatesInBounds(x, y) && !zone.getBlock(x, y).getItem(layer).isAir();
    }
    
    public boolean isSolid(int x, int y) {
        return zone.isBlockSolid(x, y);
    }
    
    public boolean isSolid(int x, int y, boolean checkAdjacents) {
        return zone.isBlockSolid(x, y, checkAdjacents);
    }
    
    public boolean isEarthy(int x, int y) {
        if(!zone.areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        return zone.getBlock(x, y).getFrontItem().isEarthy();
    }
    
    public int getEarthLayer(int y) {
        int[] depths = zone.getDepths();
        return zone.getBiome() == Biome.DEEP 
                ? y >= depths[2] ? 598 : y >= depths[1] ? 597 : y >= depths[0] ? 596 : 595
                : y >= depths[2] ? 518 : y >= depths[1] ? 517 : y >= depths[0] ? 516 : 512;
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
