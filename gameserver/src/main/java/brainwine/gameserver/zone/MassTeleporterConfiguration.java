package brainwine.gameserver.zone;

import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.util.MathUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassTeleporterConfiguration extends WorldMachineConfiguration {
    private CommandAccessLevel teleportToPlayerAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel teleportToPlaqueAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel teleportInProtectedAreaAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel summonOtherPlayerAccess = CommandAccessLevel.OWNERS;

    @Override
    protected String getDialogName() {
        return "dialogs.world_machines.teleport.configure";
    }

    public void reset() {
        teleportInProtectedAreaAccess = CommandAccessLevel.OWNERS;
        teleportToPlayerAccess = CommandAccessLevel.OWNERS;
        teleportToPlaqueAccess = CommandAccessLevel.OWNERS;
        summonOtherPlayerAccess = CommandAccessLevel.OWNERS;
    }

    @Override
    public void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException {
        reset();

        CommandAccessLevel[] arrLevels = CommandAccessLevel.values();
        if(values.containsKey("tp_player")) {
            teleportToPlayerAccess = arrLevels[MathUtils.clamp(expectInteger(values.get("tp_player")), 0, 3)];
        }

        if(values.containsKey("tp_plaque")) {
            teleportToPlaqueAccess = arrLevels[MathUtils.clamp(expectInteger(values.get("tp_plaque")), 0, 3)];
        }

        if(values.containsKey("tp_protected")) {
            teleportInProtectedAreaAccess = arrLevels[MathUtils.clamp(expectInteger(values.get("tp_protected")), 0, 3)];
        }

        if(values.containsKey("summon")) {
            summonOtherPlayerAccess = arrLevels[MathUtils.clamp(expectInteger(values.get("summon")), 0, 2)];
        }
    }

    @Override
    protected Object getValue(String key) {
        switch(key) {
            case "tp_player":
                return teleportToPlayerAccess.ordinal();
            case "tp_plaque":
                return teleportToPlaqueAccess.ordinal();
            case "tp_protected":
                return teleportInProtectedAreaAccess.ordinal();
            case "summon":
                return summonOtherPlayerAccess.ordinal();
        }

        return null;
    }

    public CommandAccessLevel getTeleportToPlayerAccess() {
        return teleportToPlayerAccess;
    }

    public CommandAccessLevel getTeleportToPlaqueAccess() {
        return teleportToPlaqueAccess;
    }

    public CommandAccessLevel getTeleportInProtectedAreaAccess() {
        return teleportInProtectedAreaAccess;
    }

    public CommandAccessLevel getSummonOtherPlayerAccess() {
        return summonOtherPlayerAccess;
    }
}
