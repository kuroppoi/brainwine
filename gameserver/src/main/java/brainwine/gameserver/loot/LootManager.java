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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.WeightedMap;
import brainwine.shared.JsonHelper;

public class LootManager {
    
    public static final int[] MIN_FREQUENCIES_BY_LUCK = {18, 15, 12, 9, 7, 5, 4, 3, 2};
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
        return lootTables.getOrDefault(category, Collections.emptyList());
    }
    
    public List<Loot> getEligibleLoot(Player player, String... categories) {
        return getEligibleLoot(player, Arrays.asList(categories));
    }
    
    public List<Loot> getEligibleLoot(Player player, Collection<String> categories) {
        int luck = player.getSkillLevel(Skill.LUCK);
        int minFrequency = luck > MIN_FREQUENCIES_BY_LUCK.length ? 1 : MIN_FREQUENCIES_BY_LUCK[luck - 1];
        List<Loot> eligibleLoot = lootTables.entrySet().stream()
                .filter(entry -> categories.contains(entry.getKey()))
                .map(Entry::getValue)
                .flatMap(Collection::stream)
                .filter(loot -> (loot.getBiome() == null || loot.getBiome() == player.getZone().getBiome())
                        && (Math.random() <= luck * 0.015 || loot.getFrequency() >= minFrequency)
                        && !player.getInventory().getWardrobe().containsAll(loot.getItems().keySet()))
                .collect(Collectors.toList());
        return eligibleLoot;
    }
    
    public Loot getRandomLoot(Player player, String... categories) {
        return getRandomLoot(player, Arrays.asList(categories));
    }
    
    public Loot getRandomLoot(Player player, Collection<String> categories) {
        return new WeightedMap<>(getEligibleLoot(player, categories), Loot::getFrequency).next();
    }
}
