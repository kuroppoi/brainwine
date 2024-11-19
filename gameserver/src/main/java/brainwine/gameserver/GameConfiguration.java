package brainwine.gameserver;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.VersionUtils;
import brainwine.shared.JsonHelper;

@SuppressWarnings("unchecked")
public class GameConfiguration {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Object> baseConfig = new HashMap<String, Object>();
    private static final Map<String, Map<String, Object>> configUpdates = new HashMap<>();
    private static final Map<String, Map<String, Object>> versionedConfigs = new HashMap<>();
    private static Yaml yaml;
    
    public static void init() {
        long startTime = System.currentTimeMillis();
        baseConfig.clear();
        configUpdates.clear();
        versionedConfigs.clear();
        logger.info(SERVER_MARKER, "Loading game configuration ...");
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(Short.MAX_VALUE);
        yaml = new Yaml(options);
        loadConfigFiles();
        loadConfigOverrides();
        logger.info(SERVER_MARKER, "Configuring ...");
        configure();
        logger.info(SERVER_MARKER, "Caching versioned configurations ...");
        cacheVersionedConfigs();
        logger.info(SERVER_MARKER, "Load complete! Took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    private static void cacheVersionedConfigs() {
        configUpdates.keySet().forEach(version -> {
            Map<String, Object> config = MapHelper.copy(baseConfig);
            
            configUpdates.forEach((version2, update) -> {
                if(VersionUtils.isGreaterOrEqualTo(version, version2)) {
                    merge(config, update);
                }
            });
            
            versionedConfigs.put(version, config);
        });
    }
    
    private static void configure() {
        // Client wants this
        MapHelper.put(baseConfig, "shop.currency", new HashMap<>());
        Map<String, Object> items = MapHelper.getMap(baseConfig, "items");
        
        // Add custom commands to the client config
        CommandManager.getCommandNames().forEach(command -> {
            MapHelper.put(baseConfig, String.format("commands.%s", command), true);
        });
        
        // Map inventory positions for items
        Map<String, int[]> inventoryPositions = new HashMap<>();
        List<Object> inventoryCategories = MapHelper.getList(baseConfig, "inventory");
        
        for(int i = 0; i < inventoryCategories.size(); i++) {
            Map<String, Object> map = (Map<String, Object>)inventoryCategories.get(i);
            List<String> itemNames = (List<String>)map.get("items");
            
            for(int j = 0; j < itemNames.size(); j++) {
                inventoryPositions.put(itemNames.get(j), new int[]{i, j});
            }
        }
        
        // Configure items
        ItemRegistry.clear();
        Map<String, Object> configV3 = configUpdates.get("3.0.0");
        
        if(items != null) {
            List<String> ignoredItems = new ArrayList<>();
            items.forEach((id, v) -> {
                Map<String, Object> config = (Map<String, Object>)v;
                
                if(!config.containsKey("code")) {
                    ignoredItems.add(id);
                    return;
                }
                
                String category = id.contains("/") ? id.substring(0, id.indexOf('/')) : "";
                config.putIfAbsent("category", category);
                config.put("id", id);
                config.putIfAbsent("title", WordUtils.capitalize(
                        (id.contains("/") ? id.substring(id.lastIndexOf('/') + 1) : id).replace("-", " ")));
                config.putIfAbsent("inventory_position", inventoryPositions.getOrDefault(id, new int[]{16, 0}));
                config.putIfAbsent("block_size", new int[]{1, 1});
                config.putIfAbsent("size", new int[]{1, 1});
                
                if(config.containsKey("use")) {
                    Map<String, Object> useConfig = (Map<String, Object>)config.get("use");
                    useConfig.remove("sound"); // TODO
                    
                    // Move change definitions to item root, but *only* for V3 clients
                    if(useConfig.containsKey("change")) {
                        MapHelper.put(configV3, String.format("items.%s.change", id), Arrays.asList(useConfig.get("change")));
                    }
                }
                
                // Map skill bonuses
                Map<String, Object> bonuses = MapHelper.getMap(config, "bonus");
                
                if(bonuses != null) {
                    Map<String, Integer> skillBonuses = new HashMap<>();
                    
                    bonuses.forEach((type, amount) -> {
                        if(amount instanceof Integer && Skill.fromId(type) != null) {
                            skillBonuses.put(type, (int)amount);
                        }
                    });
                    
                    config.put("skill_bonuses", skillBonuses);
                }
                
                // Assign layers based on category. TODO this is not accurate.
                switch(category) {
                case "base":
                case "back":
                case "front":
                case "liquid":
                    config.put("layer", category);
                    break;
                default: // Big brain or big stupid?
                    config.put("layer", "front");
                    break;
                }
                
                // Register item
                try {
                    Item item = JsonHelper.readValue(config, Item.class);
                    ItemRegistry.registerItem(item);
                } catch (Exception e) {
                    logger.fatal(SERVER_MARKER, "Failed to register item '{}'", id, e);
                    throw new RuntimeException(e); // Server SHOULD NOT attempt to start if there is a problem with the item configuration
                }
            });
            
            for(String item : ignoredItems) {
                items.remove(item);
            }
        }
        
        logger.info(SERVER_MARKER, "Successfully loaded {} item(s)", ItemRegistry.getItems().size());
    }
    
    private static void loadConfigFiles() {
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("brainwine.gameserver"))
                    .setScanners(Scanners.Resources));
            Set<String> fileNames = reflections.getResources("^(config|quests).*\\.yml$");
            
            for(String fileName : fileNames) {
                Map<String, Object> config = yaml.load(GameConfiguration.class.getResourceAsStream(String.format("/%s", fileName)));
                
                if(fileName.contains("versions")) {
                    String[] segments = fileName.replace(".yml", "").split("-");
                    
                    if(segments.length < 3) {
                        throw new IllegalArgumentException(String.format("Invalid name for config update '%s', expected format: config-versions-{version}.yml", fileName));
                    }
                    
                    String version = segments[2];
                    configUpdates.put(segments[2], (Map<String, Object>)config.get(version));
                    continue;
                }
                
                baseConfig.putAll(config);
            }
        } catch(Exception e) {
            logger.fatal(SERVER_MARKER, "Could not load configuration files", e);
            throw new RuntimeException(e); // Server SHOULD NOT attempt to start if the game configuration can't be loaded
        }
    }
    
    private static void loadConfigOverrides() {
        try {
            File configOverridesFile = new File("config_overrides.yml");
            
            if(!configOverridesFile.exists()) {
                logger.info(SERVER_MARKER, "No config overrides found. To override game config properties, put them in a file called 'config_overrides.yml'");
                return;
            }
            
            try(FileInputStream inputStream = new FileInputStream(configOverridesFile)) {
                Map<String, Object> configOverrides = yaml.load(inputStream);
                merge(baseConfig, configOverrides);
                logger.warn(SERVER_MARKER, "Configuration overrides have been loaded, proceed with caution.");
            }
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not load configuration overrides", e);
        }
    }
    
    private static void merge(Map<String, Object> dst, Map<String, Object> src) {
        src.forEach((k, v) -> {
            if(dst.containsKey(k)) {
                Object o = dst.get(k);
                
                if(o instanceof Map) {
                    // Safety check for config overrides
                    if(!(v instanceof Map)) {
                        logger.warn(SERVER_MARKER, "Configuration merger attempted to transform object '{}' into non-object value!", k);
                        return;
                    }
                    
                    merge((Map<String, Object>)o, (Map<String, Object>)v);
                } else {
                    dst.put(k, v);
                }
            } else {
                dst.put(k, v);
            }
        });
    }
    
    public static Map<String, Object> getBaseConfig() {
        return baseConfig;
    }
    
    public static Map<String, Object> getClientConfig(Player player) {
        Entry<String, Map<String, Object>> current = null;
        
        for(Entry<String, Map<String, Object>> entry : versionedConfigs.entrySet()) {
            String version = entry.getKey();
            
            if((current == null || VersionUtils.isGreaterThan(version, current.getKey())) 
                    && VersionUtils.isGreaterOrEqualTo(player.getClientVersion(), version)) {
                current = entry;
            }
        }
        
        return current == null ? baseConfig : current.getValue();
    }
}
