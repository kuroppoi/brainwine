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
        int highestPoint = surface[x];
        int lowestPoint = surface[x];
        int startX = -1;
        int endX = x;
        
        // Find foundation start and end
        for(int x1 = 0; x1 < width; x1++) {
            if(x + x1 >= surface.length) {
                return false; // Might as well return here as it won't place the prefab anyway.
            }
            
            // Shitty foundation check, doesn't take potential replacements into account. Oh well!
            if(prefab.getBlocks()[(height - 1) * width + x1].getFrontItem().isWhole()) {
                if(startX == -1) {
                    startX = x + x1;
                }
                
                endX = x + x1;
            }
        }
        
        if(startX == -1) {
            startX = x;
        }
        
        // Find highest and lowest points in the terrain across the foundation's area
        for(int x1 = startX; x1 < endX; x1++) {
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
            placeScaffolding(startX, y + height, endX - startX + 1, prefab.isRuin());
            return true;
        }
        
        return false;
    }
    
    private void placeScaffolding(int x, int y, int width, boolean ruin) {
        for(int i = x; i < x + width; i++) {
            // Don't place scaffolding unless there is a block right above it
            if(getBlock(i, y - 1).getFrontItem().isAir()) {
                continue;
            }
            
            int j = y;
            
            // Place scaffolding all the way down until it hits solid ground
            while(j < getHeight() && isAir(i, j, Layer.BASE) && !isWhole(i, j, Layer.FRONT)) {
                if(!ruin || SimplexNoise.noise2(seed, i / 8.0, j / 8.0, 2) <= 0.4) {
                    updateBlock(i, j, Layer.BACK, "back/scaffold-decayed");
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
    
    public void updateBlock(int x, int y, Layer layer, String item) {
        zone.updateBlock(x, y, layer, item);
    }
    
    public void updateBlock(int x, int y, Layer layer, String item, int mod) {
        zone.updateBlock(x, y, layer, item, mod);
    }
    
    public void updateBlock(int x, int y, Layer layer, String item, int mod, Map<String, Object> metadata) {
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
    
    public Block getBlock(int x, int y) {
        return zone.getBlock(x, y);
    }
    
    public boolean inBounds(int x, int y) {
        return zone.areCoordinatesInBounds(x, y);
    }
    
    public boolean isUnderground(int x, int y) { 
        return inBounds(x, y) && y >= zone.getSurface()[x];
    }
    
    public boolean isAir(int x, int y, Layer layer) {
        return inBounds(x, y) && getBlock(x, y).getItem(layer).isAir();
    }
    
    public boolean isOccupied(int x, int y, Layer layer) {
        return inBounds(x, y) && !getBlock(x, y).getItem(layer).isAir();
    }
    
    public boolean isWhole(int x, int y, Layer layer) {
        return inBounds(x, y) && getBlock(x, y).getItem(layer).isWhole();
    }
    
    public boolean isSolid(int x, int y) {
        return zone.isBlockSolid(x, y);
    }
    
    public boolean isSolid(int x, int y, boolean checkAdjacents) {
        return zone.isBlockSolid(x, y, checkAdjacents);
    }
    
    public boolean isEarthy(int x, int y) {
        return inBounds(x, y) && getBlock(x, y).getFrontItem().isEarthy();
    }
    
    public void setSurface(int x, int surface) {
        zone.setSurface(x, surface);
    }
    
    public int getSurface(int x) {
        return inBounds(x, 0) ? zone.getSurface()[x] : 0;
    }
    
    public String getEarthLayer(int y) {
        int[] depths = zone.getDepths();
        String[] earthLayers = zone.getBiome() == Biome.DEEP
              ? new String[] {"ground/deep-earth", "ground/deep-earth-mid", "ground/deep-earth-deep", "ground/deep-earth-very-deep"}
              : new String[] {"ground/earth", "ground/earth-mid", "ground/earth-deep", "ground/earth-very-deep"};
        
        for(int i = depths.length; i > 0; i--) {
            if(y >= depths[i - 1]) {
                return earthLayers[i];
            }
        }
        
        return earthLayers[0];
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
