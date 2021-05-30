package brainwine.gameserver.loot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.Biome;

public class LootManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, List<Loot>> lootTables = new HashMap<>();
    
    public LootManager() {
        loadLootTables();
    }
    
    private void loadLootTables() {
        logger.info("Loading loot tables ...");
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("loottables.json");
        
        try {
            if(!file.exists()) {
                Files.copy(LootManager.class.getResourceAsStream("/loottables.json"), file.toPath());
            }
            
            Map<String, List<Loot>> loot = mapper.readValue(file, new TypeReference<Map<String, List<Loot>>>(){});
            lootTables.putAll(loot);
        } catch (IOException e) {
            logger.error("Failed to load loot tables", e);
        }
    }
    
    public List<Loot> getLootTable(String category) {
        return lootTables.getOrDefault(category, Collections.emptyList());
    }
    
    public List<Loot> getEligibleLoot(int level, Biome biome, String... categories) {
        List<Loot> eligibleLoot = new ArrayList<>();
        
        for(String category : categories) {
            eligibleLoot.addAll(getLootTable(category));
        }
        
        eligibleLoot.removeIf(loot -> loot.getBiome() != null && loot.getBiome() != biome);
        
        if(Math.random() > level * 0.015) {
            int[] frequencies = {18, 15, 12, 9, 7, 5, 4, 3, 2};
            int minFrequency = level > frequencies.length ? 1 : frequencies[level - 1];
            eligibleLoot.removeIf(loot -> loot.getFrequency() < minFrequency);
        }
        
        return eligibleLoot;
    }
    
    public Loot getRandomLoot(int level, Biome biome, String... categories) {
        WeightedList<Loot> weightedLoot = new WeightedList<>();
        List<Loot> eligibleLoot = getEligibleLoot(level, biome, categories);
        
        for(Loot loot : eligibleLoot) {
            weightedLoot.addEntry(loot, loot.getFrequency());
        }
        
        return weightedLoot.next();
    }
}