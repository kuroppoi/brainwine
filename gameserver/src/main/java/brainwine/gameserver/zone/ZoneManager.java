package brainwine.gameserver.zone;

import java.io.File;
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

import com.fasterxml.jackson.databind.InjectableValues;

import brainwine.gameserver.zone.gen.AsyncZoneGenerator;
import brainwine.gameserver.zone.gen.StaticZoneGenerator;
import brainwine.shared.JsonHelper;

public class ZoneManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final AsyncZoneGenerator asyncGenerator = new AsyncZoneGenerator(this);
    private final File dataDir = new File("zones");
    private Map<String, Zone> zones = new HashMap<>();
    private Map<String, Zone> zonesByName = new HashMap<>();
    
    public ZoneManager() {
        loadZones();
        asyncGenerator.setDaemon(true);
        asyncGenerator.start();
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
        try {
            zone.save();
        } catch(Exception e) {
            logger.error("Zone save failure. id: {}", zone.getDocumentId(), e);
        }
    }
    
    private void loadZones() {
        logger.info("Loading zone data ...");
        dataDir.mkdirs();
        File[] files = dataDir.listFiles();
        
        if(files.length == 0) {
            logger.info("Generating default zone ...");
            Zone zone = StaticZoneGenerator.generateZone(Biome.PLAIN, 2000, 800);
            saveZone(zone);
            putZone(zone);
            return;
        }
        
        for(File file : files) {
            if(file.isDirectory()) {
                loadZone(file);
            }
        }
        
        logger.info("Successfully loaded {} zone(s)", zonesByName.size());
    }
    
    private void loadZone(File file) {
        String id = file.getName();
        
        try {
            File configFile = new File(file, "config.json");
            Zone zone = JsonHelper.readValue(configFile, Zone.class, new InjectableValues.Std().addValue("documentId", id));
            zone.load();
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
