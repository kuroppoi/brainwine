package brainwine.gameserver.zone.gen;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.Zone;

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
        "Crushthorn", "Tanlyward", "Ahncrace"
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
        "Canyon", "Cranwarry"
    };
    
    private static final Logger logger = LogManager.getLogger();
    private static final ZoneGenerator fallback = new ZoneGenerator();
    private static final Map<Biome, ZoneGenerator> generators = new HashMap<>();
    
    public static void init() {
        logger.info("Loading zone generator configurations ...");
        File file = new File("generators.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        
        try {
            if(!file.exists()) {
                InputStream stream = StaticZoneGenerator.class.getResourceAsStream("/generators.json");
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                stream.close();
                Files.write(file.toPath(), bytes);
            }
            
            Map<Biome, GeneratorConfig> configs = mapper.readValue(new File("generators.json"), new TypeReference<Map<Biome, GeneratorConfig>>(){});
            
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
