package brainwine.gameserver.zone;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.WeightedMap;
import brainwine.shared.JsonHelper;

/**
 * Manages plant growth in a zone.
 */
public class GrowthManager {
    
    public static final int MAX_RAIN_CYCLES = 500; // Maximum number of rain cycles that are permitted in a single growth update
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Item, Growable> growables = new HashMap<>();
    private static final Map<Biome, Map<Item, WeightedMap<Item>>> sourcesByBiome = new HashMap<>();
    private final Set<Integer> sourceIndices = new HashSet<>();
    private final Map<Item, WeightedMap<Item>> sources;
    private final Zone zone;
    
    public GrowthManager(Zone zone) {
        this.sources = sourcesByBiome.getOrDefault(zone.getBiome(), Collections.emptyMap());
        this.zone = zone;
    }
    
    public static void loadGrowthData() {
        growables.clear();
        sourcesByBiome.clear();
        
        try {
            URL url = ResourceFinder.getResourceUrl("growth.json");
            Map<String, Object> data = JsonHelper.readValue(url, new TypeReference<Map<String, Object>>(){});
            growables.putAll(JsonHelper.readValue(data.get("growables"), new TypeReference<Map<Item, Growable>>(){}));
            sourcesByBiome.putAll(JsonHelper.readValue(data.get("sources"), new TypeReference<Map<Biome, Map<Item, WeightedMap<Item>>>>(){}));
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not load growth data", e);
        }
    }
    
    /**
     * Calls {@link #updateGrowables(int, Collection)} where {@code sourceIndices} is the currently indexed growables.
     */
    public void updateGrowables(int rainCycles) {
        updateGrowables(rainCycles, sourceIndices);
    }
    
    /**
     * Updates the specified growables {@code n} times where {@code n} is the number of rain cycles.
     */
    public void updateGrowables(int rainCycles, Collection<Integer> sourceIndices) {        
        // Do nothing if zone isn't purified
        if(!zone.isPurified() && zone.getBiome() != Biome.HELL) {
            return;
        }
        
        // Do nothing if there's nothing to do... duh!
        if(rainCycles < 1 || sourceIndices.isEmpty()) {
            return;
        }
        
        // Reduce overhead by reducing the number of iterations in exchange for a growth chance boost
        rainCycles = Math.min(MAX_RAIN_CYCLES, rainCycles);
        int growthChanceBoost = Math.min(10, rainCycles);
        rainCycles /= growthChanceBoost;
        
        // Update growth for each rain cycle
        for(int i = 0; i < rainCycles; i++) {
            Iterator<Integer> iterator = sourceIndices.iterator();
            
            while(iterator.hasNext()) {
                int index = iterator.next();
                int x = index % zone.getWidth();
                int y = index / zone.getWidth();
                
                // Unindex if chunk is not loaded
                if(y == 0 || !zone.isChunkLoaded(x, y)) {
                    iterator.remove();
                    continue;
                }
                
                // Skip if sunlight can't reach this source
                if(zone.getSunlight()[x] < y) {
                    continue;
                }
                
                Block sourceBlock = zone.getBlock(x, y);
                Item sourceItem = sourceBlock.getFrontItem();
                
                // Unindex if block is not a source
                if(!sources.containsKey(sourceItem)) {
                    iterator.remove();
                    continue;
                }
                
                Block growableBlock = zone.getBlock(x, y - 1);
                Item growableItem = growableBlock.getFrontItem();
                
                // Place a random growable if block isn't occupied or try to grow it if it is a valid growable
                if(growableItem.isAir()) {
                    growableItem = sources.get(sourceItem).next();
                    
                    // Update block if item exists
                    if(growableItem != null) {
                        zone.updateBlock(x, y - 1, Layer.FRONT, growableItem);
                    }
                } else if(growables.containsKey(growableItem)) {
                    Growable growable = growables.get(growableItem);
                    int mod = growableBlock.getFrontMod();
                    
                    // Try to apply a growth stage if the plant can still grow
                    if(mod < growable.getMaxMod() && Math.random() < growable.getChance() * growthChanceBoost) {
                        zone.updateBlock(x, y - 1, Layer.FRONT, growableItem, ++mod);
                        
                        // Replace source block if max mod has been reached
                        if(growable.getReplaceSource() != null && mod >= growable.getMaxMod()) {
                            zone.updateBlock(x, y, Layer.FRONT, growable.getReplaceSource());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Attempts to index the block and returns {@code true} if the block's front item is a valid growth source.
     */
    public boolean indexBlock(int x, int y, Item item) {
        if(!sources.containsKey(item)) {
            return false;
        }
        
        sourceIndices.add(zone.getBlockIndex(x, y));
        return true;
    }
}
