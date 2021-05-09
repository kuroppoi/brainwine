package brainwine.gameserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.util.VersionUtils;

@SuppressWarnings("unchecked")
public class GameConfiguration {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Configuration baseConfig = new Configuration();
    private static final Map<String, Configuration> configUpdates = new HashMap<>();
    private static final Map<String, Configuration> versionedConfigs = new HashMap<>();
    private static Yaml yaml;
    
    public static void init() {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game configuration ...");
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
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
            Configuration config = copy(baseConfig, new TypeReference<Configuration>(){});
            
            configUpdates.forEach((version2, update) -> {
                if(VersionUtils.isGreaterOrEqualTo(version2, version2)) {
                    merge(config, update);
                }
            });
            
            versionedConfigs.put(version, config);
        });
    }
    
    private static void configure() {
        // Client wants this
        baseConfig.putObject("shop.currency", new HashMap<>());
        Map<String, Object> items = baseConfig.getMap("items");
        
        // Add custom commands to the client config
        CommandManager.getCommandNames().forEach(command -> {
            baseConfig.putObject(String.format("commands.%s", command), true);
        });
        
        // Map inventory positions for items
        Map<String, int[]> inventoryPositions = new HashMap<>();
        List<Object> inventoryCategories = baseConfig.getList("inventory");
        
        for(int i = 0; i < inventoryCategories.size(); i++) {
            Map<String, Object> map = (Map<String, Object>)inventoryCategories.get(i);
            List<String> itemNames = (List<String>)map.get("items");
            
            for(int j = 0; j < itemNames.size(); j++) {
                inventoryPositions.put(itemNames.get(j), new int[]{i, j});
            }
        }
        
        // Configure items
        Configuration configV3 = configUpdates.get("3.0.0");
        
        if(items != null) {
            List<String> ignoredItems = new ArrayList<>();
            items.forEach((name, v) -> {
                Map<String, Object> config = (Map<String, Object>)v;
                String[] segments = name.split("/", 2);
                String category = segments.length == 2 ? segments[0] : "unknown";
                config.put("id", name);
                config.putIfAbsent("title", segments.length == 2 ? segments[1] : segments[0]);
                config.putIfAbsent("category", category);
                config.putIfAbsent("inventory_position", inventoryPositions.getOrDefault(name, new int[]{16, 0}));
                config.putIfAbsent("block_size", new int[]{1, 1});
                config.putIfAbsent("size", new int[]{1, 1});
                
                if(config.containsKey("use")) {
                    Map<String, Object> useConfig = (Map<String, Object>)config.get("use");
                    useConfig.remove("sound"); // TODO
                    
                    // Move change definitions to item root, but *only* for V3 clients
                    if(useConfig.containsKey("change")) {
                        configV3.putObject(String.format("items.%s.change", name), Arrays.asList(useConfig.get("change")));
                    }
                }
                
                // Assign layers based on category. TODO this is not accurate.
                switch(category) {
                case "base":
                case "back":
                case "front":
                case "liquid":
                    config.putIfAbsent("layer", category);
                    break;
                case "ground":
                case "building":
                case "furniture":
                case "lighting":
                case "industrial":
                case "vegetation":
                case "mechanical":
                case "rubble":
                case "containers":
                case "arctic":
                    config.putIfAbsent("layer", "front");
                    break;
                }
                
                // Register item
                if(config.containsKey("code")) {
                    InjectableValues.Std injectableValues = new InjectableValues.Std();
                    injectableValues.addValue("name", name);
                    mapper.setInjectableValues(injectableValues);
                    
                    try {
                        Item item = mapper.readValue(mapper.writer().writeValueAsString(config), Item.class);
                        ItemRegistry.registerItem(item);
                    } catch (JsonProcessingException e) {
                        logger.fatal("Failed to register item {}", name, e);
                        System.exit(0);
                    }
                } else {
                    ignoredItems.add(name);
                }
            });
            
            for(String item : ignoredItems) {
                items.remove(item);
            }
        }
        
        logger.info("Successfully loaded {} items", ItemRegistry.getItems().size());
    }
    
    private static void loadConfigFiles() {
        try {
            ZipInputStream inputStream = new ZipInputStream(GameConfiguration.class.getResourceAsStream("/config.zip"));
            ZipEntry zipEntry = null;
            
            while((zipEntry = inputStream.getNextEntry()) != null) {
                Map<String, Object> config = yaml.load(inputStream);
                String name = zipEntry.getName();
                
                if(name.contains("versions")) {
                    String[] segments = name.replace(".yml", "").split("-");
                    
                    if(segments.length < 3) {
                        throw new IllegalArgumentException(String.format("Invalid name for config update '%s', expected format: config-versions-{version}.yml", name));
                    }
                    
                    String version = segments[2];
                    configUpdates.put(segments[2], new Configuration((Map<String, Object>)config.get(version)));
                    continue;
                }
                
                baseConfig.putAll(config);
            }
            
            inputStream.close();
        } catch(Exception e) {
            logger.fatal("Could not load configuration files", e);
            System.exit(-1);
        }
    }
    
    private static <T> T copy(T original, TypeReference<T> reference) {
        try {
            return mapper.readValue(mapper.writeValueAsString(original), reference);
        } catch (JsonProcessingException e) {
            logger.fatal("Copy creation failed", e);
            System.exit(-1);
        }
        
        return null;
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
    
    public static Configuration getBaseConfig() {
        return baseConfig;
    }
    
    public static Configuration getClientConfig(Player player) {
        Configuration config = baseConfig;
        
        for(Entry<String, Configuration> entry : versionedConfigs.entrySet()) {
            if(VersionUtils.isGreaterOrEqualTo(player.getClientVersion(), entry.getKey())) {
                config = entry.getValue();
            }
        }
        
        return config;
    }
}
