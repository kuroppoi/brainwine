package brainwine.gameserver.quest;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.WeightedMap;
import brainwine.shared.JsonHelper;
import io.netty.util.internal.ThreadLocalRandom;

public class RandomQuests {
    private static final Logger logger = LogManager.getLogger();
    private static Configuration configuration = new Configuration();

    private static class Configuration {
        @JsonProperty("level_max_tiers")
        private Map<Integer, Integer> levelMaxTiers = new HashMap<>();
        @JsonProperty("strings")
        Map<String, List<String>> strings = new HashMap<>();
        @JsonProperty("quests")
        List<RandomQuest> randomQuests = new ArrayList<>();
    }

    public static void loadConfiguration() {
        logger.info(SERVER_MARKER, "Loading loot tables ...");
        
        try {
            URL url = ResourceFinder.getResourceUrl("random-quests.json");
            configuration = JsonHelper.readValue(url, new TypeReference<Configuration>(){});
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load random quests", e);
        }
    }

    public static int getMaxTier(Player player) {
        if(player == null) return 1;

        int tier = 1;
        int level = player.getLevel();
        for (Map.Entry<Integer, Integer> e : configuration.levelMaxTiers.entrySet()) {
            if (level >= e.getValue() && tier < e.getKey()) {
                tier = e.getKey();
            }
        }

        return tier;
    }

    public static Quest generateRandomPlayerQuest(Player player) {
        return generateRandomPlayerQuest(ThreadLocalRandom.current(), player);
    }
    
    public static Quest generateRandomPlayerQuest(Random random, Player player) {
        int maxTier = getMaxTier(player);
        List<RandomQuest> candidates = configuration.randomQuests.stream()
                .filter(q -> q.getTier() <= maxTier)
                .collect(Collectors.toList());

        WeightedMap<RandomQuest> wm = new WeightedMap<>(
                candidates,
                RandomQuest::getFrequency
        );

        RandomQuest choice = wm.next(random);

        Quest quest = new Quest();
        quest.setId("daily_player_quest");
        choice.nextQuest(random, player, quest);

        return quest;
    }

    public static String getString(Random random, String label) {
        List<String> list = configuration.strings.get(label);

        return list.get(random.nextInt(0, list.size()));
    }
}
