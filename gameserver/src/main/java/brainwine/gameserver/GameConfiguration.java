package brainwine.gameserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.server.messages.ConfigurationMessage;

/**
 * Game Configuration.
 * Responsible for processing YAML files & loading in the necessary game logic,
 * along with providing additional data.
 * This class assumes that all configurations are present and are in a proper format, 
 * so don't modify them unless you know what you are doing!
 */
@SuppressWarnings("unchecked")
public class GameConfiguration {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, Object> configMap = new HashMap<>();
    private static final Map<String, Map<String, Object>> configUpdates = new LinkedHashMap<>();
    private static Yaml yaml;

    /**
     * Initializes the game configuration.
     * Loads all of the YAML configuration files and mashes them together into {@code configHash}
     * for use in {@link ConfigurationMessage}.
     */
    public static void init() {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game configuration ...");
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(Short.MAX_VALUE);
        yaml = new Yaml(options);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        
        try {
            ZipInputStream inputStream = new ZipInputStream(GameConfiguration.class.getResourceAsStream("/config.zip"));
            ZipEntry zipEntry = null;
            
            while((zipEntry = inputStream.getNextEntry()) != null) {
                loadConfiguration(zipEntry.getName(), inputStream);
            }
            
            inputStream.close();
        } catch(Exception e) {
            logger.fatal("Failed to load game configuration", e);
            System.exit(-1);
        }
        
        if(!configUpdates.isEmpty()) {
            logger.info("Applying configuration updates {} ...", configUpdates.keySet());
            
            for(Entry<String, Map<String, Object>> entry : configUpdates.entrySet()) {
                Map<String, Object> update = entry.getValue();
                update = (Map<String, Object>)update.get(entry.getKey());
                deepMerge(configMap, update);
            }
        }
        
        logger.info("Configuring ...");
        Map<String, Object> shop = getConfiguration("shop");
        shop.put("currency", new HashMap<>()); // Client wants this.
        
        try {
            configureItems();
        } catch (IOException e) {
            logger.fatal("After initialization failed", e);
        }
        
        logger.info("Load complete! Took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    /**
     * Automatically creates missing data for items and registers them.
     * @throws IOException
     */
    private static void configureItems() throws IOException {
        Map<String, Object> items = getConfiguration("items");
        List<String> ignoredItems = new ArrayList<>();
        
        for(Entry<String, Object> entry : items.entrySet()) {
            Map<String, Object> itemConfig = (Map<String, Object>)entry.getValue();
            String name = entry.getKey().toLowerCase();
            String category = "none";
            String[] segments = name.split("/", 2);
            
            if(segments.length == 2) {
                category = segments[0];
                itemConfig.put("category", category);
            }
            
            switch(category) {
            case "base":
            case "back":
            case "front":
            case "liquid":
                itemConfig.put("layer", category);
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
                itemConfig.put("layer", "front");
                break;
            }
            
            if(!itemConfig.containsKey("title")) {
                // TODO
                itemConfig.put("title", segments.length == 1 ? segments[0] : segments[1]);
            }
            
            // Move change definitions to item root
            if(itemConfig.containsKey("use") && !itemConfig.containsKey("change")) {
                Map<String, Object> useConfig = (Map<String, Object>)itemConfig.get("use");
                
                if(useConfig.containsKey("change")) {
                    itemConfig.put("change", Arrays.asList(useConfig.get("change")));
                }
            }
            
            if(itemConfig.containsKey("code")) {
                InjectableValues.Std injectableValues = new InjectableValues.Std();
                injectableValues.addValue("name", name);
                mapper.setInjectableValues(injectableValues);
                Item item = mapper.readValue(mapper.writer().writeValueAsString(itemConfig), Item.class);
                
                if(!ItemRegistry.registerItem(item)){
                    ignoredItems.add(name);
                }
            } else {
                ignoredItems.add(name);
            }
        }
        
        for(String ignoredItem : ignoredItems) {
            items.remove(ignoredItem);
        }
        
        logger.info("Loaded {} items, ignored {} items", ItemRegistry.getItems().size(), ignoredItems.size());
    }
    
    private static void deepMerge(Map<String, Object> dst, Map<String, Object> src) {
        for(Entry<String, Object> entry : src.entrySet()) {
            Object srcObject = entry.getValue();
            
            if(dst.containsKey(entry.getKey())) {
                Object dstObject = dst.get(entry.getKey());
                
                if(dstObject instanceof Map && srcObject instanceof Map) {
                    deepMerge((Map<String, Object>)dstObject, (Map<String, Object>)srcObject);
                } else {
                    dst.put(entry.getKey(), srcObject);
                }
            } else {
                dst.put(entry.getKey(), srcObject);
            }
        }
    }
    
    private static void loadConfiguration(String fileName, InputStream inputStream) throws IOException {
        Map<String, Object> config = yaml.load(inputStream);
        
        if(fileName.contains("versions")) {
            String[] info = fileName.split("-");
            
            if(info.length != 3) {
                throw new IOException("Invalid update format. Correct format: config-versions-{version}.yml");
            }
            
            String version = info[2].replace(".yml", "");
            configUpdates.put(version, config);
        } else {
            for(Entry<String, Object> entry : config.entrySet()) {
                configMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    private static Map<String, Object> getConfiguration(String name) {
        return (Map<String, Object>)configMap.getOrDefault(name, new HashMap<>());
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ConfigurationMessage}.
     */
    public static Map<String, Object> getClientConfig() {
        return configMap;
    }
}
