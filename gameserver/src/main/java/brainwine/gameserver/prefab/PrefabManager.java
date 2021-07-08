package brainwine.gameserver.prefab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.unpacker.BufferUnpacker;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.shared.JsonHelper;

public class PrefabManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final File dataDir = new File("prefabs");
    private final Map<String, Prefab> prefabs = new HashMap<>();
    
    public PrefabManager() {
        loadPrefabs();
    }
    
    private void loadPrefabs() {
        logger.info("Loading prefabs ...");
        
        if(!dataDir.exists()) {
            logger.info("Copying default prefabs ...");
            dataDir.mkdirs();
            Reflections reflections = new Reflections("prefabs", new ResourcesScanner());
            Set<String> fileNames = reflections.getResources(x -> true);
            
            for(String fileName : fileNames) {
                File outputFile = new File(fileName);
                outputFile.getParentFile().mkdirs();
                
                try {
                    Files.copy(PrefabManager.class.getResourceAsStream(String.format("/%s", fileName)), outputFile.toPath());
                } catch (IOException e) {
                    logger.error("Could not copy default prefabs", e);
                }
            }
        }
        
        File[] files = dataDir.listFiles();
        
        for(File file : files) {
            if(file.isDirectory()) {
                loadPrefab(file);
            }
        }
        
        logger.info("Successfully loaded {} prefab(s)", prefabs.size());
    }
    
    private void loadPrefab(File file) {
        String name = file.getName();
        File configFile = new File(file, "config.json");
        
        try {
            Prefab prefab = JsonHelper.readValue(configFile, Prefab.class);
            BufferUnpacker unpacker = MessagePackHelper.readFile(new File(file, "blocks.cmp"));
            unpacker.read(prefab);
            unpacker.close();
            prefabs.put(name, prefab);
        } catch(Exception e) {
            logger.error("Could not load prefab {}:", name, e);
        }
    }
    
    public void registerPrefab(String name, Prefab structure) throws Exception {
        if(prefabs.containsKey(name)) {
            logger.warn("Duplicate prefab name: {}", name);
            return;
        }
        
        savePrefab(name, structure);
        prefabs.put(name, structure);
    }
    
    private void savePrefab(String name, Prefab structure) throws Exception {
        File outputDir = new File(dataDir, name);
        outputDir.mkdirs();
        MessagePackHelper.writeToFile(new File(outputDir, "blocks.cmp"), structure);
        JsonHelper.writeValue(new File(outputDir, "config.json"), structure);
    }
    
    public Prefab getPrefab(String name) {
        return prefabs.get(name);
    }
}
