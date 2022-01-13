package brainwine.gameserver.prefab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.serialization.BlockDeserializer;
import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.Block;
import brainwine.shared.JsonHelper;

public class PrefabManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .registerModule(new SimpleModule()
                    .addDeserializer(Block.class, BlockDeserializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE));
    private final File dataDir = new File("prefabs");
    private final Map<String, Prefab> prefabs = new HashMap<>();
    
    public PrefabManager() {
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
        File legacyBlocksFile = new File(file, "blocks.cmp");
        File blocksFile = new File(file, "blocks.dat");
        
        try {
            PrefabBlockData blockData = null;
            
            if(legacyBlocksFile.exists() && !blocksFile.exists()) {
                logger.info("Updating blocks file for prefab '{}' ...", name);
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(
                        ZipUtils.inflateBytes(Files.readAllBytes(legacyBlocksFile.toPath())));
                int width = unpacker.unpackInt();
                int height = unpacker.unpackInt();
                Block[] blocks = new Block[unpacker.unpackArrayHeader() / 3];
                
                for(int i = 0; i < blocks.length; i++) {
                    blocks[i] = new Block(unpacker.unpackInt(), unpacker.unpackInt(), unpacker.unpackInt());
                }
                
                blockData = new PrefabBlockData(width, height, blocks);
                Files.write(blocksFile.toPath(), ZipUtils.deflateBytes(mapper.writeValueAsBytes(blockData)));
                legacyBlocksFile.delete();
            } else {
                blockData = mapper.readValue(ZipUtils.inflateBytes(Files.readAllBytes(blocksFile.toPath())), PrefabBlockData.class);
            }
            
            PrefabConfig config = JsonHelper.readValue(new File(file, "config.json"), PrefabConfig.class);
            prefabs.put(name, new Prefab(config, blockData));
        } catch(Exception e) {
            logger.error("Could not load prefab {}:", name, e);
        }
    }
    
    public void addPrefab(String name, Prefab prefab) throws Exception {
        if(prefabs.containsKey(name)) {
            logger.warn("Duplicate prefab name: {}", name);
            return;
        }
        
        File prefabDir = new File(dataDir, name);
        prefabDir.mkdirs();
        JsonHelper.writeValue(new File(prefabDir, "config.json"), new PrefabConfig(prefab));
        Files.write(new File(prefabDir, "blocks.dat").toPath(), ZipUtils.deflateBytes(mapper.writeValueAsBytes(new PrefabBlockData(prefab))));
        prefabs.put(name, prefab);
    }
    
    public Prefab getPrefab(String name) {
        return prefabs.get(name);
    }
}
