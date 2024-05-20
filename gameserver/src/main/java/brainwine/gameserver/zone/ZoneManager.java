package brainwine.gameserver.zone;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import brainwine.gameserver.entity.npc.NpcData;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.gen.ZoneGenerator;
import brainwine.shared.JsonHelper;

public class ZoneManager {
    
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private final File dataDir = new File("zones");
    private Map<String, Zone> zones = new HashMap<>();
    private Map<String, Zone> zonesByName = new HashMap<>();
    private long lastZoneGenerationTime = System.currentTimeMillis();
    private boolean generatingZone = false;
        
    public ZoneManager() {
        logger.info(SERVER_MARKER, "Loading zone data ...");
        dataDir.mkdirs();
        
        for(File file : dataDir.listFiles()) {
            if(file.isDirectory()) {
                loadZone(file);
            }
        }
        
        logger.info(SERVER_MARKER, "Successfully loaded {} zone(s)", zonesByName.size());
    }
    
    public void tryGenerateDefaultZone() {
        if(!zones.isEmpty()) {
            return;
        }
        
        logger.info(SERVER_MARKER, "No zones were loaded. Generating default zone ...");
        ZoneGenerator generator = ZoneGenerator.getZoneGenerator(Biome.PLAIN);
        
        if(generator == null) {
            logger.warn(SERVER_MARKER, "No generator for plain biomes was found! The default generator will be used.");
            generator = ZoneGenerator.getDefaultZoneGenerator();
        }
        
        Zone zone = generator.generateZone(Biome.PLAIN);
        addZone(zone);
    }
    
    public void tick(float deltaTime) {
        for(Zone zone : getZones()) {
            zone.tick(deltaTime);
        }

        long timeSinceLastGeneration = (System.currentTimeMillis() - lastZoneGenerationTime) / 1000;

        // zero players interval has to be greater than the min generation interval
        final long MIN_GENERATION_INTERVAL_SECONDS = 10 * 60;
        final long GENERATION_INTERVAL_ZERO_PLAYERS_SECONDS = 30 * 60;
        // player count influence has to be positive and a greater value means
        // more players are needed for a given increase in generation rate
        final long PLAYER_COUNT_INFLUENCE = 16;

        if (!generatingZone && timeSinceLastGeneration > MIN_GENERATION_INTERVAL_SECONDS) {
            int playerCount = zones.values().stream().map(Zone::getPlayerCount).reduce(Integer::sum).orElse(0);
            long requiredInterval = Math.max(
                MIN_GENERATION_INTERVAL_SECONDS,
                GENERATION_INTERVAL_ZERO_PLAYERS_SECONDS - (playerCount - 1) * (GENERATION_INTERVAL_ZERO_PLAYERS_SECONDS - MIN_GENERATION_INTERVAL_SECONDS) / PLAYER_COUNT_INFLUENCE
            );

            if (timeSinceLastGeneration > requiredInterval) {
                if (shouldGenerateUnexploredZone() && !generatingZone) {
                    generatingZone = true;
                    Biome biome = Biome.getRandomBiome();
                    ZoneGenerator generator = ZoneGenerator.getZoneGenerator(biome);
                    generator.generateZoneAsync(biome, zone -> {
                        if (zone != null) {
                            this.addZone(zone);
                            lastZoneGenerationTime = System.currentTimeMillis();
                        } else {
                            logger.warn(SERVER_MARKER, "Automatic zone generation failed. See the previous logs for more information.");
                        }
                        generatingZone = false;
                    });
                }
            }
        }
    }
    
    public void onShutdown() {
        for(Zone zone : zones.values()) {
            saveZone(zone);
            zone.getChunkManager().closeStream();
        }
    }
    
    private void loadZone(File file) {
        String id = file.getName();
        File dataFile = new File(file, "zone.dat");
        File legacyDataFile = new File(file, "shape.cmp");
        File configFile = new File(file, "config.json");
        File metaBlocksFile = new File(file, "metablocks.json");
        File charactersFile = new File(file, "characters.json");
        
        try {            
            if(legacyDataFile.exists() && !dataFile.exists()) {
                throw new IOException("Zone data format is outdated. Please try to load this zone with an older server version to update it.");
            }
            
            ZoneDataFile data = mapper.readValue(ZipUtils.inflateBytes(Files.readAllBytes(dataFile.toPath())), ZoneDataFile.class);
            ZoneConfigFile config = JsonHelper.readValue(configFile, ZoneConfigFile.class);
            Zone zone = new Zone(id, config, data);
            
            // Load meta blocks
            if(metaBlocksFile.exists()) {
                zone.setMetaBlocks(JsonHelper.readList(metaBlocksFile, MetaBlock.class));
            }
            
            // Load characters
            if(charactersFile.exists()) {
                zone.spawnPersistentNpcs(JsonHelper.readList(charactersFile, NpcData.class));
            }
            
            zone.simulate(ChronoUnit.SECONDS.between(config.getLastActiveDate(), OffsetDateTime.now()));
            addZone(zone);
        } catch (Exception e) {
            logger.error(SERVER_MARKER, "Zone load failure. id: {}", id, e);
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
            // Serialize everything before writing to disk to minimize risk of data corruption if something goes wrong
            byte[] charactersBytes = JsonHelper.writeValueAsBytes(zone.getPersistentNpcs().stream().map(NpcData::new).collect(Collectors.toList()));
            byte[] metaBlocksBytes = JsonHelper.writeValueAsBytes(zone.getMetaBlocks());
            byte[] configBytes = JsonHelper.writeValueAsBytes(new ZoneConfigFile(zone));
            byte[] dataBytes = ZipUtils.deflateBytes(mapper.writeValueAsBytes(new ZoneDataFile(zone)));
            
            // Write data to files
            zone.saveChunks();
            Files.write(new File(file, "characters.json").toPath(), charactersBytes);
            Files.write(new File(file, "metablocks.json").toPath(), metaBlocksBytes);
            Files.write(new File(file, "config.json").toPath(), configBytes);
            Files.write(new File(file, "zone.dat").toPath(), dataBytes);
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Zone save failure. id: {}", zone.getDocumentId(), e);
        }
    }
    
    public void addZone(Zone zone) {
        String id = zone.getDocumentId();
        String name = zone.getName();
        
        if(zonesByName.containsKey(name.toLowerCase())) {
            logger.warn(SERVER_MARKER, "Duplicate name {} for zone id {}", name, id);
            return;
        }
        
        zones.put(id, zone);
        zonesByName.put(name.toLowerCase(), zone);
    }

    /**Should the game create a new world because all the worlds are established?
     *
     * @return true iff the game should create a new world at next opportunity
     */
    public boolean shouldGenerateUnexploredZone() {
        return getZones().stream().allMatch(zone -> zone.getExplorationProgress() >= 0.4);
    }
    
    public Zone getZone(String id) {
        return zones.get(id);
    }
    
    public Zone getZoneByName(String name) {
        return zonesByName.get(name.toLowerCase());
    }
    
    public Zone getRandomZone() {
        return getRandomZone(null);
    }
    
    public Zone getRandomZone(Predicate<Zone> predicate) {
        List<Zone> zones = searchZones(predicate);
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
    
    public int getZoneCount() {
        return zones.size();
    }
    
    public Collection<Zone> getZones() {
        return zonesByName.values();
    }
}
