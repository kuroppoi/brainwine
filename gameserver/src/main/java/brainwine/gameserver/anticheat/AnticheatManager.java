package brainwine.gameserver.anticheat;

import brainwine.gameserver.resource.ResourceFinder;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

public class AnticheatManager {
    private static AnticheatConfig config = new AnticheatConfig();
    private static final Logger logger = LogManager.getLogger();

    public static void loadConfig() {
        logger.info(SERVER_MARKER, "Loading anti-cheat ...");

        try {
            URL url = ResourceFinder.getResourceUrl("anticheat.json");
            config = JsonHelper.readValue(url, new TypeReference<AnticheatConfig>(){});
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load anti-cheat config", e);
        }
    }

    public static AnticheatConfig getConfig() {
        return config;
    }
}
