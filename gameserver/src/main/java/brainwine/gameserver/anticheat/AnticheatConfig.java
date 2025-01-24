package brainwine.gameserver.anticheat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnticheatConfig {
    @JsonProperty("exploder_farm")
    private ExploderFarm exploderFarm = new ExploderFarm();

    public ExploderFarm getExploderFarm() {
        return exploderFarm;
    }
}
