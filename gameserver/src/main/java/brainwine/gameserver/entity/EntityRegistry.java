package brainwine.gameserver.entity;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.InjectableValues;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

public class EntityRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, EntityConfig> entities = new HashMap<>();
    
    public static void init() {
        entities.clear();
        Map<String, Map<String, Object>> entityConfigs = MapHelper.getMap(GameConfiguration.getBaseConfig(), "entities");
        
        if(entityConfigs == null) {
            logger.warn(SERVER_MARKER, "No entity configurations exist!");
            return;
        }
        
        for(Entry<String, Map<String, Object>> entry : entityConfigs.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> config = entry.getValue();
            
            if(!config.containsKey("code")) {
                continue;
            }
            
            try {
                registerEntityConfig(name, JsonHelper.readValue(config, EntityConfig.class, 
                        new InjectableValues.Std().addValue("name", name)));
            } catch(Exception e) {
                logger.error(SERVER_MARKER, "Could not deserialize entity config for entity '{}'", name, e);
            }
        }
        
        int entityCount = entities.size();
        logger.info(SERVER_MARKER, "Successfully loaded {} entit{}", entityCount, entityCount == 1 ? "y" : "ies");
    }
    
    public static void registerEntityConfig(String name, EntityConfig config) {
        if(entities.containsKey(name)) {
            logger.warn(SERVER_MARKER, "Attempted to register entity with name '{}' twice", name);
            return;
        }
        
        entities.put(name, config);
    }
    
    public static EntityConfig getEntityConfig(String name) {
        return entities.get(name);
    }
}
