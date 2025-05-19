package brainwine.gameserver.zone;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

public class ZoneActivityManager {
    private static final Logger logger = LogManager.getLogger();

    private Map<ZoneActivity, ZoneActivityConfiguration> configs = new HashMap<>();

    private void loadConfiguration() {
        try {
            logger.info(SERVER_MARKER, "Loading zone activity configuration...");
            URL url = ResourceFinder.getResourceUrl("activities.json");
            Map<ZoneActivity, ZoneActivityConfiguration> loot = JsonHelper.readValue(url, new TypeReference<Map<ZoneActivity, ZoneActivityConfiguration>>(){});
            configs.putAll(loot);
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load zone activity configuration", e);
        }
    }

    public ZoneActivityManager() {
        loadConfiguration();
    }

    public Zone getPrimaryZone(ZoneActivity activity) {
        ZoneActivityConfiguration config = configs.get(activity);

        if(config == null) return null;

        if(config.getPrimaryZone() == null) return null;

        return GameServer.getInstance().getZoneManager().getZone(config.getPrimaryZone());
    }

    public Map<String, Integer> getPlayerItemLimits(ZoneActivity activity) {
        ZoneActivityConfiguration config = configs.get(activity);

        if(config == null) return Collections.emptyMap();

        return config.getPlayerItemLimits();
    }

    public Map<String, Integer> getPlayerInventoryLimits(ZoneActivity activity) {
        ZoneActivityConfiguration config = configs.get(activity);

        if(config == null) return Collections.emptyMap();

        return config.getPlayerInventoryLimits();
    }

}
