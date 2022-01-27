package brainwine.gameserver.zone;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.gen.AsyncZoneGenerator;
import brainwine.gameserver.zone.gen.StaticZoneGenerator;
import brainwine.shared.JsonHelper;

public class ZoneManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final AsyncZoneGenerator asyncGenerator = new AsyncZoneGenerator(this);
    private final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
    private final File dataDir = new File("zones");
    private Map<String, Zone> zones = new HashMap<>();
    private Map<String, Zone> zonesByName = new HashMap<>();
    
    public ZoneManager() {
        logger.info("Loading zone data ...");
        dataDir.mkdirs();
        
        for(File file : dataDir.listFiles()) {
            if(file.isDirectory()) {
                loadZone(file);
            }
        }
        
        if(zones.isEmpty()) {
            logger.info("No zones were loaded. Generating default zone ...");
            Zone zone = StaticZoneGenerator.generateZone(Biome.PLAIN, 2000, 600);
            saveZone(zone);
            putZone(zone);
        } else {
            logger.info("Successfully loaded {} zone(s)", zonesByName.size());
        }
        
        logger.info("Starting zone generator thread ...");
        asyncGenerator.setDaemon(true);
        asyncGenerator.start();
    }
    
    public void tick(float deltaTime) {
        for(Zone zone : getZones()) {
            zone.tick(deltaTime);
        }
    }
    
    private void loadZone(File file) {
        String id = file.getName();
        File dataFile = new File(file, "zone.dat");
        File shapeFile = new File(file, "shape.cmp");
        
        try {
            ZoneData data = null;
            
            if(shapeFile.exists() && !dataFile.exists()) {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(ZipUtils.inflateBytes(Files.readAllBytes(shapeFile.toPath())));
                int[] surface = new int[unpacker.unpackArrayHeader()];
                
                for(int i = 0; i < surface.length; i++) {
                    surface[i] = unpacker.unpackInt();
                }
                
                int[] sunlight = new int[unpacker.unpackArrayHeader()];
                
                for(int i = 0; i < sunlight.length; i++) {
                    sunlight[i] = unpacker.unpackInt();
                }
                
                int[] pendingSunlight = new int[unpacker.unpackArrayHeader()];
                
                for(int i = 0; i < pendingSunlight.length; i++) {
                    pendingSunlight[i] = unpacker.unpackInt();
                }
                
                boolean[] chunksExplored = new boolean[unpacker.unpackArrayHeader()];
                
                for(int i = 0; i < pendingSunlight.length; i++) {
                    chunksExplored[i] = unpacker.unpackBoolean();
                }
                
                data = new ZoneData(surface, sunlight, pendingSunlight, chunksExplored);
                mapper.writeValue(dataFile, data);
                shapeFile.delete();
            } else {
                data = mapper.readValue(ZipUtils.inflateBytes(Files.readAllBytes(dataFile.toPath())), ZoneData.class);
            }
            
            ZoneConfig config = JsonHelper.readValue(new File(file, "config.json"), ZoneConfig.class);
            Zone zone = new Zone(id, config, data);
            zone.setMetaBlocks(JsonHelper.readList(new File(file, "metablocks.json"), MetaBlock.class));
            putZone(zone);
        } catch (Exception e) {
            logger.error("Zone load failure. id: {}", id, e);
        }
    }
    
    public void saveZones() {
        for(Zone zone : getZones()) {
            saveZone(zone);
        }
    }
    
    public void saveZone(Zone zone) {
        File file = zone.getDirectory();
        file.mkdirs();
        
        try {
            zone.saveModifiedChunks();
            ZoneConfig config = JsonHelper.readValue(zone, ZoneConfig.class);
            ZoneData data = JsonHelper.readValue(zone, ZoneData.class);
            JsonHelper.writeValue(new File(file, "metablocks.json"), zone.getMetaBlocks());
            JsonHelper.writeValue(new File(file, "config.json"), config);
            Files.write(new File(file, "zone.dat").toPath(), ZipUtils.deflateBytes(mapper.writeValueAsBytes(data)));
        } catch(Exception e) {
            logger.error("Zone save failure. id: {}", zone.getDocumentId(), e);
        }
    }
    
    private void putZone(Zone zone) {
        String id = zone.getDocumentId();
        String name = zone.getName();
        
        if(zonesByName.containsKey(name.toLowerCase())) {
            logger.warn("Duplicate name {} for zone id {}", name, id);
            return;
        }
        
        zones.put(id, zone);
        zonesByName.put(name.toLowerCase(), zone);
    }
    
    public void generateZoneAsync(Biome biome, int width, int height, int seed, Consumer<Zone> callback) {
        asyncGenerator.generateZone(biome, width, height, seed, zone -> {
            if(zone != null) {
                putZone(zone);
            }
            
            callback.accept(zone);
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
    
    public List<Zone> searchZones(Predicate<Zone> predicate) {
        return searchZones(predicate, null);
    }
    
    public List<Zone> searchZones(Comparator<Zone> comparator) {
        return searchZones(null, comparator);
    }
    
    public List<Zone> searchZones(Predicate<Zone> predicate, Comparator<Zone> comparator) {
        List<Zone> result = new ArrayList<>();
        Collection<Zone> zones = this.zones.values();
        
        for(Zone zone : zones) {
            if(predicate == null || predicate.test(zone)) {
                result.add(zone);
            }
        }
        
        if(comparator != null) {
            result.sort(comparator);
        }
        
        if(result.size() > 50) {
            Iterator<Zone> it = result.listIterator(50);
            
            while(it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        
        return result;
    }
    
    public Collection<Zone> getZones() {
        return zonesByName.values();
    }
}
