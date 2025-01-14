package brainwine.gameserver.loot;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Biome;
import brainwine.shared.JsonHelper;

public class LootManager {
    
    public static final double LEVELS_PER_BONUS_ROLL = 6.0;
    public static final int MAX_BONUS_ROLLS = 20;
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, List<Loot>> lootTables = new HashMap<>();
    
    public LootManager() {
        loadLootTables();
    }
    
    private void loadLootTables() {
        logger.info(SERVER_MARKER, "Loading loot tables ...");
        
        try {
            URL url = ResourceFinder.getResourceUrl("loottables.json");
            Map<String, List<Loot>> loot = JsonHelper.readValue(url, new TypeReference<Map<String, List<Loot>>>(){});
            lootTables.putAll(loot);
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load loot tables", e);
        }
    }
    
    public List<Loot> getLootTable(String category) {
        return lootTables.get(category);
    }
    
    public Set<String> getLootCategories() {
        return Collections.unmodifiableSet(lootTables.keySet());
    }
    
    public List<Loot> getEligibleLoot(Biome biome, Set<Item> ignore, String... categories) {
        return getEligibleLoot(biome, ignore, Arrays.asList(categories));
    }
    
    public List<Loot> getEligibleLoot(Biome biome, Set<Item> ignore, Collection<String> categories) {
        List<Loot> eligibleLoot = lootTables.entrySet().stream()
                .filter(entry -> categories.contains(entry.getKey()))
                .map(Entry::getValue)
                .flatMap(Collection::stream)
                .filter(loot -> (loot.getBiome() == null || loot.getBiome() == biome) && !ignore.containsAll(loot.getItems().keySet()))
                .collect(Collectors.toList());
        return eligibleLoot;
    }
    
    public Loot getRandomLoot(Player player, String... categories) {
        return getRandomLoot(player, Arrays.asList(categories));
    }
    
    public Loot getRandomLoot(Player player, Collection<String> categories) {
        return getRandomLoot(player.getTotalSkillLevel(Skill.LUCK), player.getZone().getBiome(), player.getInventory().getWardrobe(), categories);
    }
    
    public Loot getRandomLoot(int luck, Biome biome, Set<Item> ignore, String... categories) {
        return getRandomLoot(luck, biome, ignore, Arrays.asList(categories));
    }
    
    public Loot getRandomLoot(int luck, Biome biome, Set<Item> ignore, Collection<String> categories) {
        WeightedMap<Loot> map = new WeightedMap<>(getEligibleLoot(biome, ignore, categories), Loot::getFrequency);
        Loot loot = map.next();
        double rolls = (luck - 1) / LEVELS_PER_BONUS_ROLL;
        int bonusRolls = Math.min(MAX_BONUS_ROLLS, (int)rolls);
        
        // Turn remainder into a chance to get an extra bonus roll
        if(bonusRolls < MAX_BONUS_ROLLS && Math.random() < (rolls - bonusRolls)) {
            bonusRolls++;
        }
        
        // Perform bonus rolls and return the lowest frequency loot
        for(int i = 0; i < bonusRolls; i++) {
            Loot next = map.next();
            
            if(loot == null || (next != null && next.getFrequency() < loot.getFrequency())) {
                loot = next;
            }
        }
        
        return loot;
    }
}
