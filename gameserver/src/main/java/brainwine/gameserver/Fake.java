package brainwine.gameserver;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import static brainwine.shared.LogMarkers.SERVER_MARKER;
import brainwine.gameserver.util.MapHelper;

public class Fake {
    private static final Logger logger = LogManager.getLogger();
    public static Map<String, Object> fake = null;

    public static enum Type {
        SALUTATION,
        JOKE,
    }

    public static enum Degree {
        UNFRIENDLY(-1),
        NEUTRAL(0),
        FRIENDLY(1);

        public final int value;

        private Degree(int value) {
            this.value = value;
        }
    }

    /** Load fake.yml and if successful cache its contents into the fake variable.
     */
    public static void loadFake() {
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(Short.MAX_VALUE);
        Yaml yaml = new Yaml(options);

        try {
            fake = yaml.load(Fake.class.getResourceAsStream("/fake.yml"));
        } catch (Exception e) {
            logger.error(SERVER_MARKER, "Failed to load fakes", e);
            return;
        }
    }

    /** Get cached fake.yml contents or attempt to load it and return.
     * 
     * @return cached or newly loaded fake.yml contents or null if unsuccessful
     */
    private static Map<String, Object> getFake() {
        if(fake == null) {
            loadFake();
        }

        return fake;
    }

    /** Get fake from a list whose type is not defined in the Fake.Type enum.
     * 
     * @param <T> the type of object to return
     * @param listKey the path to the list in configuration (automatically prefixed with fake.)
     * @return random selection
     */
    public static <T> T get(String listKey) {
        List<T> list = MapHelper.getList(GameConfiguration.getBaseConfig(), "fake." + listKey);
        if(list != null) {
            return pickFromList(list);
        }else {
            throw new NoSuchElementException();
        }
    }

    /** Get a fake of known type. Also see the overload get(type, degree).
     * 
     * @param type the type of fake
     * @return random selection
     */
    public static String get(Type type) {
        return get(type, Degree.NEUTRAL);
    }

    /** Get a fake of known type and of given mood, if applicable. Only use with SALUTATION.
     * 
     * @param type the type of fake
     * @param degree the degree of the fake
     * @return random selection
     */
    public static String get(Type type, Degree degree) {
        return switch (type) {
            case SALUTATION -> switch (degree) {
                case UNFRIENDLY -> pickFromList("fake.salutations.unfriendly");
                case NEUTRAL -> pickFromList("fake.salutations.neutral");
                case FRIENDLY -> pickFromList("fake.salutations.friendly");
            };
            case JOKE -> pickFromList("fake.jokes");
        };
    }

    private static String pickFromList(String path) {
        Map<String, Object> myFake = getFake();

        if (myFake == null) {
            return "missingno";
        } else {
            return pickFromList(MapHelper.getList(getFake(), path));
        }
    }

    /** Pick one item randomly from the given list. It uses Math.random() internally.
     * @param list the list
     * @return random selection
     */
    private static <T> T pickFromList(List<T> list) {
        int length = list.size();
        int index = (int) Math.round(Math.floor(length * Math.random()));
        return list.get(index);
    }
    
}
