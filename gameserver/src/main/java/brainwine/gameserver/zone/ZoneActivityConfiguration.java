package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ZoneActivityConfiguration {
    @JsonProperty("player_item_limits")
    private Map<String, Integer> playerItemLimits = new HashMap<>();

    public Map<String, Integer> getPlayerItemLimits() {
        return playerItemLimits;
    }
}
