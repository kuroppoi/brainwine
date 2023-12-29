package brainwine.gameserver.zone.gen;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.ResourceUtils;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.gen.tasks.CaveGeneratorTask;
import brainwine.gameserver.zone.gen.tasks.DecorGeneratorTask;
import brainwine.gameserver.zone.gen.tasks.GeneratorTask;
import brainwine.gameserver.zone.gen.tasks.StructureGeneratorTask;
import brainwine.gameserver.zone.gen.tasks.TerrainGeneratorTask;
import brainwine.shared.JsonHelper;

public class ZoneGenerator {
    
    // TODO Collect more names and create a name generator that's actually proper lmao
    private static final String[] FIRST_NAMES = {
        "Malvern", "Tralee", "Horncastle", "Old", "Westwood",
        "Citta", "Tadley", "Mossley", "West", "East",
        "North", "South", "Wadpen", "Githam", "Soatnust",
        "Highworth", "Creakynip", "Upper", "Lower", "Cannock",
        "Dovercourt", "Limerick", "Pickering", "Glumshed", "Crusthack",
        "Osyltyr", "Aberstaple", "New", "Stroud", "Crumclum",
        "Crumsidle", "Bankswund", "Fiddletrast", "Bournpan", "St.",
        "Funderbost", "Bexwoddly", "Pilkingheld", "Wittlepen", "Rabbitbleaker",
        "Griffingumby", "Guilthead", "Bigglelund", "Bunnymold", "Rosesidle",
        "Crushthorn", "Tanlyward", "Ahncrace", "Pilkingking", "Dingstrath",
        "Axebury", "Ginglingtap", "Ballybibby", "Shadehoven"
    };
    
    private static final String[] LAST_NAMES = {
        "Falls", "Alloa", "Glen", "Way", "Dolente",
        "Peak", "Heights", "Creek", "Banffshire", "Chagford",
        "Gorge", "Valley", "Catacombs", "Depths", "Mines",
        "Crickbridge", "Guildbost", "Pits", "Vaults", "Ruins",
        "Dell", "Keep", "Chatterdin", "Scrimmance", "Gitwick",
        "Ridge", "Alresford", "Place", "Bridge", "Glade",
        "Mill", "Court", "Dooftory", "Hills", "Specklewint",
        "Grove", "Aylesbury", "Wagwouth", "Russetcumby", "Point",
        "Canyon", "Cranwarry", "Bluff", "Passage", "Crantippy",
        "Kerbodome", "Dale", "Cemetery"
    };
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, ZoneGenerator> generators = new HashMap<>();
    private static final ZoneGenerator defaultGenerator = new ZoneGenerator();
    private static AsyncZoneGenerator asyncGenerator;
    private final GeneratorTask terrainGenerator;
    private final GeneratorTask caveGenerator;
    private final GeneratorTask decorGenerator;
    private final GeneratorTask structureGenerator;
    
    public ZoneGenerator() {
        this(new GeneratorConfig());
    }
    
    public ZoneGenerator(GeneratorConfig config) {
        terrainGenerator = new TerrainGeneratorTask(config);
        caveGenerator = new CaveGeneratorTask(config);
        decorGenerator = new DecorGeneratorTask(config);
        structureGenerator = new StructureGeneratorTask(config);
    }
    
    public static void init() {
        generators.clear();
        logger.info(SERVER_MARKER, "Loading zone generator configurations ...");
        ResourceUtils.copyDefaults("generators/");
        File dataDir = new File("generators");
        
        if(dataDir.isDirectory()) {
            for(File file : dataDir.listFiles()) {
                try {
                    String name = ResourceUtils.removeFileSuffix(file.getName()).toLowerCase();
                    
                    if(generators.containsKey(name)) {
                        logger.warn(SERVER_MARKER, "Duplicate generator config name '{}'", name);
                        continue;
                    }
                    
                    GeneratorConfig config = JsonHelper.readValue(file, GeneratorConfig.class);
                    generators.put(name, new ZoneGenerator(config));
                } catch(Exception e) {
                    logger.error(SERVER_MARKER, "Failed to load generator config '{}'", file.getName(), e);
                }
            }
        }
        
        logger.info(SERVER_MARKER, "Starting async zone generator thread ...");
        asyncGenerator = new AsyncZoneGenerator();
        asyncGenerator.start();
    }
    
    /**
     * Calls {@code stopAsyncZoneGenerator(false)}
     * 
     * @see #stopAsyncZoneGenerator(boolean)
     */
    public static void stopAsyncZoneGenerator() {
        stopAsyncZoneGenerator(false);
    }
    
    /**
     * Gracefully stops the current zone generator thread.
     * 
     * @param wait If true, the thread that calls this function will be blocked until the zone generator thread dies.
     */
    public static void stopAsyncZoneGenerator(boolean wait) {
        if(asyncGenerator != null && asyncGenerator.isAlive()) {
            logger.info(SERVER_MARKER, "Stopping async zone generator thread ...");
            asyncGenerator.stopGracefully();
            
            if(wait) {
                try {
                    asyncGenerator.join();
                } catch(InterruptedException e) {
                    logger.error(SERVER_MARKER, "Wait for zone generator thread death interrupted", e);
                }
            }
        }
    }
    
    public static ZoneGenerator getZoneGenerator(String name) {
        return generators.get(name.toLowerCase());
    }
    
    public static ZoneGenerator getZoneGenerator(Biome biome) {
        return getZoneGenerator(biome.toString());
    }
    
    public static ZoneGenerator getDefaultZoneGenerator() {
        return defaultGenerator;
    }
    
    public Zone generateZone() {
        return generateZone(Biome.getRandomBiome());
    }
    
    public Zone generateZone(Biome biome) {
        return generateZone(biome, 2000, 600);
    }
    
    public Zone generateZone(Biome biome, int width, int height) {
        return generateZone(biome, width, height, getRandomSeed());
    }
    
    public Zone generateZone(Biome biome, int width, int height, int seed) {
        String id = generateDocumentId(seed);
        String name = getRandomName();
        int retryCount = 0;
        
        while(GameServer.getInstance().getZoneManager().getZoneByName(name) != null) {
            if(retryCount >= 10) {
                name = id;
                logger.warn(SERVER_MARKER, "Could not generate a unique name for zone {}", id);
                break;
            }
            
            name = getRandomName();
            retryCount++;
        }
        
        Zone zone = new Zone(id, name, biome, width, height);
        GeneratorContext ctx = new GeneratorContext(zone, seed);
        terrainGenerator.generate(ctx);
        caveGenerator.generate(ctx);
        decorGenerator.generate(ctx);
        structureGenerator.generate(ctx);
        
        // Bedrock
        for(int x = 0; x < width; x++) {
            ctx.updateBlock(x, height - 1, Layer.FRONT, "ground/bedrock");
        }
        
        return zone;
    }
    
    public void generateZoneAsync(Consumer<Zone> callback) {
        generateZoneAsync(Biome.getRandomBiome(), callback);
    }
    
    public void generateZoneAsync(Biome biome, Consumer<Zone> callback) {
        generateZoneAsync(biome, 2000, 600, callback);
    }
    
    public void generateZoneAsync(Biome biome, int width, int height, Consumer<Zone> callback) {
        generateZoneAsync(biome, width, height, getRandomSeed(), callback);
    }
    
    public void generateZoneAsync(Biome biome, int width, int height, int seed, Consumer<Zone> callback) {
        asyncGenerator.addTask(this, biome, width, height, seed, callback);
    }
    
    private static String generateDocumentId(int seed) {
        Random random = new Random();
        long mostSigBits = (((long)seed) << 32) | (random.nextInt() & 0xFFFFFFFFL);
        long leastSigBits = random.nextLong();
        return new UUID(mostSigBits, leastSigBits).toString();
    }
    
    private static String getRandomName() {
        String firstName = FIRST_NAMES[(int)(Math.random() * FIRST_NAMES.length)];
        String lastName = LAST_NAMES[(int)(Math.random() * LAST_NAMES.length)];
        return firstName + " " + lastName;
    }
    
    private static int getRandomSeed() {
        return (int)(Math.random() * Integer.MAX_VALUE);
    }
}
