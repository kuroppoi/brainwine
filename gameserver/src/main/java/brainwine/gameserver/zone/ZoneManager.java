package brainwine.gameserver.zone;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.unpacker.BufferUnpacker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.zone.gen.ZoneGenerator;

public class ZoneManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final File dataDir = new File("zones");
    private Map<String, Zone> zones = new HashMap<>();
    private Map<String, Zone> zonesByName = new HashMap<>();
    
    public ZoneManager() {
        dataDir.mkdirs();
        loadZones();
    }
    
    public void tick() {
        for(Zone zone : getZones()) {
            zone.tick();
        }
    }
    
    public void saveZones() {
        for(Zone zone : zonesByName.values()) {
            saveZone(zone);
        }
    }
    
    public void saveZone(Zone zone) {
        String id = zone.getDocumentId();
        ObjectMapper mapper = new ObjectMapper();
        File zoneDir = new File(dataDir, id);
        zoneDir.mkdirs();
        
        try {
            File configFile = new File(zoneDir, "config.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, zone);
            File shapeFile = new File(zoneDir, "shape.cmp");
            MessagePackHelper.writeToFile(shapeFile, zone.getSurface(), zone.getSunlight(), zone.getPendingSunlight(), zone.getChunksExplored());
            File metaFile = new File(zoneDir, "metablocks.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(metaFile, zone.getMetaBlocks());
        } catch(Exception e) {
            logger.error("Zone save failure. id: {}", id, e);
        }
        
        zone.saveModifiedChunks(); // TODO
    }
    
    private void loadZones() {
        logger.info("Loading zone data ...");
        File[] files = dataDir.listFiles();
        
        if(files.length == 0) {
            logger.info("Generating default zone ...");
            Zone zone = StaticZoneGenerator.generateZone(Biome.PLAIN, 2000, 800);
            saveZone(zone);
            putZone(zone);
            return;
        }
        
        for(File file : files) {
            loadZone(file);
        }
        
        logger.info("Successfully loaded {} zone(s)", zonesByName.size());
    }
    
    private void loadZone(File file) {
        String id = file.getName();
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectableValues = new InjectableValues.Std();
        injectableValues.addValue("documentId", id);
        mapper.setInjectableValues(injectableValues);
        
        try {
            File configFile = new File(file, "config.json");
            Zone zone = mapper.readValue(configFile, Zone.class);
            BufferUnpacker unpacker = MessagePackHelper.readFile(new File(file, "shape.cmp"));
            zone.setSurface(unpacker.read(int[].class));
            zone.setSunlight(unpacker.read(int[].class));
            zone.setPendingSunlight(unpacker.read(int[].class));
            zone.setChunksExplored(unpacker.read(boolean[].class));
            File metaFile = new File(file, "metablocks.json");
            List<MetaBlock> metaBlocks = mapper.readValue(metaFile, new TypeReference<List<MetaBlock>>(){});
            
            for(MetaBlock metaBlock : metaBlocks) {
                zone.setMetaBlock(metaBlock.getX(), metaBlock.getY(), metaBlock);
            }
            
            putZone(zone);
        } catch (Exception e) {
            logger.error("Zone load failure. id: {}", id, e);
        }
    }
    
    private void putZone(Zone zone) {
        String id = zone.getDocumentId();
        String name = zone.getName();
        
        if(zonesByName.containsKey(name)) {
            logger.warn("Duplicate name {} for zone id {}", name, id);
            return;
        }
        
        zones.put(id, zone);
        zonesByName.put(name.toLowerCase(), zone);
    }
    
    public void generateZoneAsync(Biome biome, int width, int height, int seed, AsyncZoneGeneratedHandler callback) {
        asyncGenerator.generateZone(biome, width, height, seed, zone -> {
            putZone(zone);
            callback.handle(zone);
        });
    }
    
    public Zone getZone(String id) {
        return zones.get(id);
    }
    
    public Zone getZoneByName(String name) {
        return zonesByName.get(name.toLowerCase());
    }
    
    public Zone getRandomZone() {
        List<Zone> zones = new ArrayList<>();
        zones.addAll(getZones());
        return zones.get((int)(Math.random() * zones.size()));
    }
    
    public Collection<Zone> getRandomZones(int amount){
        Map<Integer, Zone> map = new HashMap<>();
        List<Zone> zones = new ArrayList<Zone>();
        zones.addAll(getZones());
        
        for(int i = 0; i < amount; i++) {
            int listIndex = 0;
            
            while(map.containsKey(listIndex = (int)(Math.random() * zones.size())));
            
            map.put(listIndex, zones.get(listIndex));
            
            if(i + 1 > amount) {
                break;
            }
        }
        
        return map.values();
    }
    
    public Collection<Zone> getZones() {
        return zonesByName.values();
    }
}
