package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ZoneActivityConfiguration {
    @JsonProperty("primary_zone")
    private String primaryZone = null;
    @JsonProperty("player_item_limits")
    private Map<String, Integer> playerItemLimits = new HashMap<>();
    @JsonProperty("player_inventory_limits")
    private Map<String, Integer> playerInventoryLimits = new HashMap<>();

    public String getPrimaryZone() {
        return primaryZone;
    }

    public Map<String, Integer> getPlayerItemLimits() {
        return playerItemLimits;
    }

    public Map<String, Integer> getPlayerInventoryLimits() {
        return playerInventoryLimits;
    }
}
