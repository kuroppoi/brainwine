package brainwine.gameserver.prefab;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.serialization.BlockDeserializer;
import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.util.ResourceUtils;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.Block;
import brainwine.shared.JsonHelper;

public class PrefabManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = JsonMapper.builder(new MessagePackFactory())
            .addModule(new SimpleModule()
                    .addDeserializer(Block.class, BlockDeserializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE))
            .build();
    private final File dataDir = new File("prefabs");
    private final Map<String, Prefab> prefabs = new HashMap<>();
    
    public PrefabManager() {
        logger.info("Loading prefabs ...");
        ResourceUtils.copyDefaults("prefabs/");
        
        if(dataDir.isDirectory()) {
            for(File file : dataDir.listFiles()) {
                if(file.isDirectory()) {
                    loadPrefab(file);
                }
            }
        }
        
        logger.info("Successfully loaded {} prefab(s)", prefabs.size());
    }
    
    private void loadPrefab(File file) {
        String name = file.getPath().substring(dataDir.getPath().length() + 1).replace(File.separatorChar, '/');
        File legacyBlocksFile = new File(file, "blocks.cmp");
        File configFile = new File(file, "config.json");
        File blocksFile = new File(file, "blocks.dat");
        
        try {
            PrefabBlocksFile blockData = null;
            
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
                
                blockData = new PrefabBlocksFile(width, height, blocks);
                Files.write(blocksFile.toPath(), ZipUtils.deflateBytes(mapper.writeValueAsBytes(blockData)));
                legacyBlocksFile.delete();
            } else {
                // If no config or blocks file exists we will assume that this is a category directory.
                // TODO maybe create a separate function for all this?
                if(!configFile.exists() || !blocksFile.exists()) {
                    for(File childFile : file.listFiles()) {
                        if(childFile.isDirectory()) {
                            loadPrefab(childFile);
                        }
                    }
                    
                    return;
                }
                
                blockData = mapper.readValue(ZipUtils.inflateBytes(Files.readAllBytes(blocksFile.toPath())), PrefabBlocksFile.class);
            }
            
            PrefabConfigFile config = JsonHelper.readValue(configFile, PrefabConfigFile.class);
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
        JsonHelper.writeValue(new File(prefabDir, "config.json"), new PrefabConfigFile(prefab));
        Files.write(new File(prefabDir, "blocks.dat").toPath(), ZipUtils.deflateBytes(mapper.writeValueAsBytes(new PrefabBlocksFile(prefab))));
        prefabs.put(name, prefab);
    }
    
    public Prefab getPrefab(String name) {
        return prefabs.get(name);
    }
}
