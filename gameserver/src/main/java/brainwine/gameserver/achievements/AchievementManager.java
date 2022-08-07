package brainwine.gameserver.achievements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

public class AchievementManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Achievement> achievements = new HashMap<>();
    private static final Set<String> unknownTypeIds = new HashSet<>();
    private static boolean initialized;
    
    public static void loadAchievements() {
        if(initialized) {
            logger.warn("Already initialized!");
            return;
        }
        
        logger.info("Loading achievements ...");
        Map<String, Map<String, Object>> achievementConfigs = MapHelper.getMap(GameConfiguration.getBaseConfig(), "achievements");
        
        if(achievementConfigs == null) {
            logger.warn("No achievement configurations exist!");
            return;
        }
        
        for(Entry<String, Map<String, Object>> entry : achievementConfigs.entrySet()) {     
            String title = entry.getKey();
            Map<String, Object> config = entry.getValue();
            
            // TODO mastery does not display correctly on v2 clients without doing this.
            config.remove("tier", 1);
            
            // Try and deserialize using the title as the type if one isn't specified.
            config.putIfAbsent("type", title);
            
            try {
                registerAchievement(JsonHelper.readValue(config, Achievement.class, 
                        new InjectableValues.Std().addValue("title", title)));
            } catch(MismatchedInputException e) {
                unknownTypeIds.add(MapHelper.getString(config, "type"));
            } catch(Exception e) {
                logger.error("Could not deserialize achievement '{}'", title, e);
            }
        }
        
        if(!unknownTypeIds.isEmpty()) {
            logger.warn("Some achievements could not be loaded due to missing implementations: {}", unknownTypeIds);
        }
        
        int achievementCount = achievements.size();
        logger.info("Successfully loaded {} achievement{}", achievementCount, achievementCount == 1 ? "" : "s");
        initialized = true;
    }
    
    public static void registerAchievement(Achievement achievement) {
        String title = achievement.getTitle();
        
        if(getAchievement(title) != null) {
            logger.warn("Attempted to register duplicate achievement '{}'", title);
            return;
        }
        
        achievements.put(title.toLowerCase(), achievement);
    }
    
    public static Achievement getAchievement(String title) {
        return achievements.get(title.toLowerCase());
    }
    
    public static Collection<Achievement> getAchievements() {
        return Collections.unmodifiableCollection(achievements.values());
    }
}
