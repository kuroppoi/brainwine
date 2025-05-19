package brainwine.gameserver.zone;

import static brainwine.gameserver.player.NotificationType.SYSTEM;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.npc.NpcData;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.gen.ZoneGenerator;
import brainwine.shared.JsonHelper;
import brainwine.shared.TokenGenerator;

public class ZoneManager {
    private final double ZONE_EXPLORATION_THRESHOLD = 0.25;
    private final double UNEXPLORED_ZONES_AT_A_TIME = 1;
    // zero players interval has to be greater than the min generation interval
    final double MIN_GENERATION_INTERVAL_SECONDS = 30 * 60;
    final double GENERATION_INTERVAL_ZERO_PLAYERS_SECONDS = 30 * 60;
    // player count influence has to be positive and a greater value means
    // more players are needed for a given increase in generation rate
    final double PLAYER_COUNT_INFLUENCE = 16;

    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private final File dataDir = new File("zones");
    private Map<String, Zone> zones = new HashMap<>();
    private Map<String, Zone> zonesByName = new HashMap<>();
    private Map<String, Zone> entryCodes = new HashMap<>();
    private long lastZoneGenerationTime = System.currentTimeMillis();
    private boolean generatingZone = false;
    private Set<String> unexploredZones = new HashSet<>();
    private Biome lastGeneratedBiome = Biome.PLAIN;

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
            if(zone.isTicking()) zone.tick(deltaTime);
        }

        tryGenerateUnexploredZone();
    }

    public void tryGenerateUnexploredZone() {
        // Return if a zone is already being generated
        if(generatingZone) return;

        long currentTime = System.currentTimeMillis();
        double timeSinceLastGeneration = (currentTime - lastZoneGenerationTime) / 1000.0;

        // Check if sufficient time has passed since last generation
        if(timeSinceLastGeneration < MIN_GENERATION_INTERVAL_SECONDS) return;

        int playerCount = GameServer.getInstance().getPlayerManager().getOnlinePlayerCount();
        double requiredInterval = Math.max(MIN_GENERATION_INTERVAL_SECONDS, MathUtils.lerp(
                GENERATION_INTERVAL_ZERO_PLAYERS_SECONDS,
                MIN_GENERATION_INTERVAL_SECONDS,
                (playerCount - 1) / PLAYER_COUNT_INFLUENCE));

        if(timeSinceLastGeneration < requiredInterval) return;

        if(shouldGenerateUnexploredZone()) {
            List<Biome> biomeOptions = Arrays.stream(Biome.values()).collect(Collectors.toList());

            biomeOptions.remove(lastGeneratedBiome);
            if(lastGeneratedBiome == Biome.HELL || lastGeneratedBiome == Biome.DEEP) {
                biomeOptions.remove(Biome.HELL);
                biomeOptions.remove(Biome.DEEP);
            }

            lastGeneratedBiome = biomeOptions.get((int)(biomeOptions.size() * Math.random()));

            ZoneGenerator generator = ZoneGenerator.getZoneGenerator(lastGeneratedBiome);
            generatingZone = true;
            lastZoneGenerationTime = System.currentTimeMillis();
            generator.generateZoneAsync(lastGeneratedBiome, zone -> {
                if (zone != null) {
                    this.addZone(zone);
                    GameServer.getInstance().getPusher().handleZoneDiscovered(zone);
                    if(GameServer.getInstance().getPlayerManager() != null) for(Player player : GameServer.getInstance().getPlayerManager().getPlayers()) {
                        player.notify(String.format("A new zone has been discovered! Check out \"%s\"!", zone.getName()), SYSTEM);
                    }
                } else {
                    logger.warn(SERVER_MARKER, "Automatic zone generation failed. See the previous logs for more information.");
                }
                generatingZone = false;
            });
        }
    }

    /**
     * Should the automatic zone generator generate a new zone?
     *
     * @return {@code true} if all unowned worlds are at least 40% explored, otherwise {@code false}.
     */
    public boolean shouldGenerateUnexploredZone() {
        unexploredZones.removeIf(zone -> (!shouldTrackExplorationOfZone(getZone(zone))) || checkExplorationOfZone(getZone(zone)));

        return unexploredZones.size() < UNEXPLORED_ZONES_AT_A_TIME;
    }

    public boolean checkExplorationOfZone(Zone zone) {
        return zone.getExplorationProgress() >= ZONE_EXPLORATION_THRESHOLD;
    }

    public boolean shouldTrackExplorationOfZone(Zone zone) {
        return zone != null
                && !zone.isOwned()
                && zone.getBiome() != Biome.HELL && zone.getBiome() != Biome.DEEP;
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
        zones.values().stream().filter(Zone::isModified).forEach(this::saveZone);
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
            zone.setModified(false);
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
        if(shouldTrackExplorationOfZone(zone) && !checkExplorationOfZone(zone)) {
            unexploredZones.add(zone.getDocumentId());
        }

        if(zone.hasEntryCode()) {
            entryCodes.put(zone.getEntryCode(), zone);
        }
    }

    public void deleteZone(Zone zone) {
        zone.freeze("This zone is being deleted.");

        File folder = new File(dataDir, zone.getDocumentId());
        if(folder.isDirectory()) {
            folder.delete();
        }

        zones.remove(zone.getDocumentId());
        zonesByName.remove(zone.getName());
    }
    
    /**
     * Renames the specified zone and re-indexes it.
     * 
     * @return {@code true} if the renaming was successful, otherwise {@code false}.
     */
    @SuppressWarnings("deprecation")
    public boolean renameZone(Zone zone, String name) {
        if(doesZoneExist(name)) {
            return false; // Return false if name is already taken
        }
        
        if(!zonesByName.remove(zone.getName().toLowerCase(), zone)) {
            return false; // Sanity check
        }
        
        zone.setName(name);
        zonesByName.put(name.toLowerCase(), zone);
        return true;
    }
    
    /**
     * Generates a new entry code for the specified zone and re-indexes it.
     *
     * @return {@code true} if the entry code was generated successfully, otherwise {@code false}.
     */
    public boolean issueEntryCode(Zone zone) {
        String entryCode = String.format("z%s", TokenGenerator.generateToken(6, entryCodes::containsKey));
        String currentCode = zone.getEntryCode();

        if(entryCode == null) {
            return false;
        }

        if(currentCode != null && !entryCodes.remove(currentCode, zone)) {
            logger.warn(SERVER_MARKER, "Could not unindex entry code {} for zone {}", currentCode, zone.getDocumentId());
        }

        zone.setEntryCode(entryCode);
        entryCodes.put(entryCode, zone);
        return true;
    }

    public Zone getZone(String id) {
        return zones.get(id);
    }
    
    public boolean doesZoneExist(String name) {
        return zonesByName.containsKey(name.toLowerCase());
    }
    
    public Zone getZoneByName(String name) {
        return zonesByName.get(name.toLowerCase());
    }
    
    public Zone getZoneByEntryCode(String entryCode) {
        return entryCodes.get(entryCode);
    }

    /**
     * @return The primary tutorial zone configured in activities.json.
     */
    public Zone findTutorialZone() {
        Zone zone = GameServer.getInstance().getZoneActivityManager().getPrimaryZone(ZoneActivity.TUTORIAL);
        return zone != null ? zone : findBeginnerZone();
    }

    /**
     * @return A public, non-owned, recently-generated temperate world (with players if possible) or {@code null} if no such world exists.
     */
    public Zone findBeginnerZone() {
        return zones.values().stream()
                .filter(zone -> zone.isPublic() && !zone.isOwned() && zone.isUnexplored() && zone.getBiome() == Biome.PLAIN)
                .sorted((a, b) -> b.getCreationDate().compareTo(a.getCreationDate()))
                .limit(50)
                .sorted((a, b) -> Integer.compare(b.getPlayerCount(), a.getPlayerCount())) 
                .findFirst().orElse(null);
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
