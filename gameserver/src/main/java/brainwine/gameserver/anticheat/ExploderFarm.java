package brainwine.gameserver.anticheat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExploderFarm {
    @JsonProperty
    private boolean enabled = false;
    @JsonProperty("loot_counter_max")
    private int lootCounterMax = 1;
    @JsonProperty("xp_factor")
    private double xpFactor = 1.0;

    public boolean isEnabled() {
        return enabled;
    }

    public int getLootCounterMax() {
        return lootCounterMax;
    }

    public double getXpFactor() {
        return xpFactor;
    }
}
