package brainwine.gameserver.prefab;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.serialization.BlockDeserializer;
import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.Block;
import brainwine.shared.JsonHelper;

public class PrefabManager {
    
    public static final String PREFAB_DIRECTORY_NAME = "prefabs";
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = JsonMapper.builder(new MessagePackFactory())
            .addModule(new SimpleModule()
                    .addDeserializer(Block.class, BlockDeserializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE)).build();
    private final File dataDirectory = new File(PREFAB_DIRECTORY_NAME);
    private final Map<String, Prefab> prefabs = new LinkedHashMap<>();
    
    public PrefabManager() {
        loadPrefabs();
    }
    
    private void loadPrefabs() {
        logger.info(SERVER_MARKER, "Loading prefabs ...");
        
        // Fetch prefab names and load prefabs
        ResourceFinder.getResources(PREFAB_DIRECTORY_NAME).stream()
                .map(x -> x.getParentDirectoryName())
                .filter(x -> x.length() > PREFAB_DIRECTORY_NAME.length())
                .map(x -> x.substring(PREFAB_DIRECTORY_NAME.length() + 1))
                .distinct()
                .forEach(this::loadPrefab);
        
        logger.info(SERVER_MARKER, "Successfully loaded {} prefab{}", prefabs.size(), prefabs.size() == 1 ? "" : "s");
    }
    
    private void loadPrefab(String name) {
        try {
            URL configUrl = ResourceFinder.getResourceUrl(String.format("prefabs/%s/config.json", name));
            URL blocksUrl = ResourceFinder.getResourceUrl(String.format("prefabs/%s/blocks.dat", name));
            PrefabConfigFile config = JsonHelper.readValue(configUrl, PrefabConfigFile.class);
            PrefabBlocksFile blockData = null;
            
            // Load block data
            try(InflaterInputStream inputStream = new InflaterInputStream(blocksUrl.openStream())) {
                blockData = mapper.readValue(inputStream, PrefabBlocksFile.class);
            }
            
            // Add prefab
            prefabs.put(name, new Prefab(name, config, blockData));
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not load prefab '{}'", name, e);
        }
    }
    
    public boolean createPrefab(Prefab prefab) throws IOException {
        return createPrefab(prefab, false);
    }
    
    public boolean createPrefab(Prefab prefab, boolean overwrite) throws IOException {
        String name = prefab.getName();
        
        // Do nothing if name already exists and overwriting is not allowed
        if(!overwrite && prefabExists(name)) {
            return false;
        }
        
        // Serialize & write data
        byte[] configBytes = JsonHelper.writeValueAsBytes(new PrefabConfigFile(prefab));
        byte[] blockBytes = ZipUtils.deflateBytes(mapper.writeValueAsBytes(new PrefabBlocksFile(prefab)));
        File outputDirectory = new File(dataDirectory, name.toLowerCase());
        outputDirectory.mkdirs();
        Files.write(new File(outputDirectory, "config.json").toPath(), configBytes);
        Files.write(new File(outputDirectory, "blocks.dat").toPath(), blockBytes);
        
        // Index prefab
        prefabs.put(name, prefab);
        return true;
    }
    
    public boolean prefabExists(String name) {
        return prefabs.containsKey(name);
    }
    
    public Prefab getPrefab(String name) {
        return prefabs.get(name.toLowerCase());
    }
    
    public Collection<Prefab> getPrefabs() {
        return Collections.unmodifiableCollection(prefabs.values());
    }
}
