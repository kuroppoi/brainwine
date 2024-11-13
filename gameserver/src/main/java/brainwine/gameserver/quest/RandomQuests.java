package brainwine.gameserver.quest;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import brainwine.gameserver.util.randomobject.ObjectMapperProvider;
import brainwine.gameserver.util.randomobject.RandomInteger;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.WeightedMap;
import io.netty.util.internal.ThreadLocalRandom;

public class RandomQuests {
    private static final Logger logger = LogManager.getLogger();
    private static Configuration configuration = new Configuration();

    public static class Configuration {
        @JsonProperty("level_max_tiers")
        private Map<Integer, Integer> levelMaxTiers = new HashMap<>();
        @JsonProperty("strings")
        Map<String, List<String>> strings = new HashMap<>();
        @JsonProperty("quests")
        List<RandomQuest> randomQuests = new ArrayList<>();
        @JsonProperty("daily_quest_interval")
        String dailyQuestInterval = "24h";
        @JsonProperty("daily_quest_count")
        int dailyQuestCount = 5;
    }

    public static class MapperProvider implements ObjectMapperProvider {
        @Override
        public ObjectMapper get() {
            return JsonHelper.MAPPER;
        }
    }

    public static void loadConfiguration() {
        logger.info(SERVER_MARKER, "Loading random quest configuration...");
        
        try {
            URL url = ResourceFinder.getResourceUrl("random-quests.json");
            configuration = JsonHelper.MAPPER.readValue(url, new TypeReference<Configuration>(){});

            logger.info(String.format("Successfully loaded %d random quests.", configuration.randomQuests.size()));
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load random quests", e);
        }
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static int getMaxTier(Player player) {
        if(player == null) return 1;

        int tier = 1;
        int level = player.getLevel();
        for(Map.Entry<Integer, Integer> e : configuration.levelMaxTiers.entrySet()) {
            if(level >= e.getValue() && tier < e.getKey()) {
                tier = e.getKey();
            }
        }

        return tier;
    }

    public static List<Quest> generateRandomPlayerQuests(Player player, int n) {
        return generateRandomPlayerQuests(ThreadLocalRandom.current(), player, n);
    }
    
    public static List<Quest> generateRandomPlayerQuests(Random random, Player player, int n) {
        if(configuration.randomQuests.isEmpty()) {
            return null;
        }

        int maxTier = getMaxTier(player);
        List<RandomQuest> candidates = configuration.randomQuests.stream()
                .filter(q -> q.getTier() <= maxTier)
                .collect(Collectors.toList());

        WeightedMap<RandomQuest> wm = new WeightedMap<>(
                candidates,
                RandomQuest::getFrequency
        );

        List<RandomQuest> choices = wm.nextN(random, n);

        return choices.stream().map(rq -> rq.nextQuest(random, player)).collect(Collectors.toList());
    }

    public static RandomQuestReward getDefaultRandomQuestReward() {
        return new RandomQuestReward(new RandomInteger(100), new RandomInteger(0), null);
    }

    public static String getString(Random random, String label) {
        List<String> list = configuration.strings.get(label);

        return list.get(random.nextInt(list.size()));
    }
}
