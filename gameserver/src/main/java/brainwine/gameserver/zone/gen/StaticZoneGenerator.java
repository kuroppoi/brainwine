package brainwine.gameserver.zone.gen;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.shared.JsonHelper;

public class StaticZoneGenerator {
    
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
    private static final ZoneGenerator fallback = new ZoneGenerator();
    private static final Map<Biome, ZoneGenerator> generators = new HashMap<>();
    
    public static void init() {
        logger.info("Loading zone generator configurations ...");
        File file = new File("generators.json");
        
        try {
            if(!file.exists()) {
                Files.copy(StaticZoneGenerator.class.getResourceAsStream("/generators.json"), file.toPath());
            }
            
            Map<Biome, GeneratorConfig> configs = JsonHelper.readValue(new File("generators.json"), new TypeReference<Map<Biome, GeneratorConfig>>(){});
            
            for(Entry<Biome, GeneratorConfig> entry : configs.entrySet()) {
                generators.put(entry.getKey(), new ZoneGenerator(entry.getValue()));
            }
        } catch(Exception e) {
            logger.error("Could not load generator configurations", e);
        }
    }
    
    public static Zone generateZone(Biome biome, int width, int height) {
        return generateZone(biome, width, height, (int)(Math.random() * Integer.MAX_VALUE));
    }
    
    public static Zone generateZone(Biome biome, int width, int height, int seed) {
        String firstName = FIRST_NAMES[(int)(Math.random() * FIRST_NAMES.length)];
        String lastName = LAST_NAMES[(int)(Math.random() * LAST_NAMES.length)];
        String name = firstName + " " + lastName;
        Zone zone = new Zone(generateDocumentId(seed), name, biome, width, height);
        GeneratorContext ctx = new GeneratorContext(zone, seed);
        ZoneGenerator generator = generators.getOrDefault(biome, fallback);
        generator.generate(ctx);
        return zone;
    }
    
    private static String generateDocumentId(int seed) {
        Random random = new Random();
        long mostSigBits = (((long)seed) << 32) | (random.nextInt() & 0xFFFFFFFFL);
        long leastSigBits = random.nextLong();
        return new UUID(mostSigBits, leastSigBits).toString();
    }
}
