package brainwine.gameserver;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
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
        logger.info("Loading game configuration ...");
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(Short.MAX_VALUE);
        yaml = new Yaml(options);
        loadConfigFiles();
        logger.info("Configuring ...");
        configure();
        logger.info("Caching versioned configurations ...");
        cacheVersionedConfigs();
        logger.info("Load complete! Took {} milliseconds", System.currentTimeMillis() - startTime);
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
            items.forEach((name, v) -> {
                Map<String, Object> config = (Map<String, Object>)v;
                
                if(!config.containsKey("code")) {
                    ignoredItems.add(name);
                    return;
                }
                
                String category = name.contains("/") ? name.substring(0, name.indexOf('/')) : "";
                config.putIfAbsent("category", category);
                config.put("id", name);
                config.putIfAbsent("title", WordUtils.capitalize(
                        (name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name).replace("-", " ")));
                config.putIfAbsent("inventory_position", inventoryPositions.getOrDefault(name, new int[]{16, 0}));
                config.putIfAbsent("block_size", new int[]{1, 1});
                config.putIfAbsent("size", new int[]{1, 1});
                
                if(config.containsKey("use")) {
                    Map<String, Object> useConfig = (Map<String, Object>)config.get("use");
                    useConfig.remove("sound"); // TODO
                    
                    // Move change definitions to item root, but *only* for V3 clients
                    if(useConfig.containsKey("change")) {
                        MapHelper.put(configV3, String.format("items.%s.change", name), Arrays.asList(useConfig.get("change")));
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
                    Item item = JsonHelper.readValue(config, Item.class, new InjectableValues.Std().addValue("name", name));
                    ItemRegistry.registerItem(item);
                } catch (JsonProcessingException e) {
                    logger.fatal("Failed to register item {}", name, e);
                    System.exit(0);
                }
            });
            
            for(String item : ignoredItems) {
                items.remove(item);
            }
        }
        
        logger.info("Successfully loaded {} item(s)", ItemRegistry.getItems().size());
    }
    
    private static void loadConfigFiles() {
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("brainwine.gameserver"))
                    .setScanners(Scanners.Resources));
            Set<String> fileNames = reflections.getResources("^config.*\\.yml$");
            
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
            logger.fatal("Could not load configuration files", e);
            System.exit(-1);
        }
    }
    
    private static void merge(Map<String, Object> dst, Map<String, Object> src) {
        src.forEach((k, v) -> {
            if(dst.containsKey(k)) {
                Object o = dst.get(k);
                
                if(o instanceof Map && v instanceof Map) {
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
