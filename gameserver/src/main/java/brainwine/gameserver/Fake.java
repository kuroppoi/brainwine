package brainwine.gameserver;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

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

    /** Load fake.json and if successful cache its contents into the fake variable.
     */
    public static void loadFake() {
        logger.info(SERVER_MARKER, "Loading fakes ...");
        
        try {
            URL url = ResourceFinder.getResourceUrl("fake.json");
            fake = JsonHelper.readValue(url, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load fakes", e);
        }
    }

    /** Get cached fake.json contents or attempt to load it and return.
     * 
     * @return cached or newly loaded fake.json contents or null if unsuccessful
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
        List<T> list = MapHelper.getList(getFake(), "fake." + listKey);
        
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
        if(type == Type.SALUTATION) {
            if(degree == Degree.UNFRIENDLY) {
                return pickFromList("fake.salutations.unfriendly");
            }
            if(degree == Degree.FRIENDLY) {
                return pickFromList("fake.salutations.friendly");
            }

            return pickFromList("fake.salutations.neutral");
        }
        
        if(type == Type.JOKE) {
            return pickFromList("fake.jokes");
        }

        return "";
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
    public static <T> T pickFromList(List<T> list) {
        int length = list.size();
        int index = (int) Math.round(Math.floor(length * Math.random()));
        return list.get(index);
    }
    
}
