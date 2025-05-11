package brainwine.gameserver.zone;

import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.util.MathUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassSpawnerConfiguration extends WorldMachineConfiguration {
    int difficulty = 3;
    boolean mawSpawningEnabled = true;
    boolean areaSpawningEnabled = true;
    CommandAccessLevel evokeAccess = CommandAccessLevel.OWNERS;

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return isEnabled("machines/mass-spawner");
    }

    @Override
    protected String getDialogName() {
        return "dialogs.world_machines.spawner.configure";
    }

    public void reset(Zone zone) {
        difficulty = 3;
        mawSpawningEnabled = true;
        areaSpawningEnabled = true;
        evokeAccess = CommandAccessLevel.OWNERS;
        onUpdate(zone);
    }

    @Override
    protected void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException {
        difficulty = 3;
        mawSpawningEnabled = true;
        areaSpawningEnabled = true;
        evokeAccess = CommandAccessLevel.OWNERS;

        if(values.get("hostility") != null) {
            difficulty = MathUtils.clamp(2 * expectInteger(values.get("hostility")) + 1, 1, 5);
        }

        if(values.get("maw_spawning") != null) {
            mawSpawningEnabled = expectInteger(values.get("maw_spawning")) == 0;
        }

        if(values.get("area_spawning") != null) {
            areaSpawningEnabled = expectInteger(values.get("area_spawning")) == 0;
        }

        if(values.get("evoke") != null) {
            evokeAccess = CommandAccessLevel.values()[MathUtils.clamp(expectInteger(values.get("evoke")), 0, 2)];
        }

        onUpdate(zone);
    }

    @Override
    protected Object getValue(String key) {
        switch(key) {
            case "hostility":
                return difficulty / 2;
            case "maw_spawning":
                return mawSpawningEnabled ? 0 : 1;
            case "area_spawning":
                return areaSpawningEnabled ? 0 : 1;
            case "evoke":
                return evokeAccess.ordinal();
        }

        return null;
    }

    private void onUpdate(Zone zone) {
        zone.getEntityManager().updateSpawnRates();
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isMawSpawningEnabled() {
        return mawSpawningEnabled;
    }

    public boolean isAreaSpawningEnabled() {
        return areaSpawningEnabled;
    }

    public CommandAccessLevel getEvokeAccess() {
        return evokeAccess;
    }
}
