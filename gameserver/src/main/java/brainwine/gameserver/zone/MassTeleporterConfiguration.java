package brainwine.gameserver.zone;

import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.player.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassTeleporterConfiguration {
    private CommandAccessLevel teleportToPlayerAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel teleportToPlaqueAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel teleportInProtectedAreaAccess = CommandAccessLevel.OWNERS;
    private CommandAccessLevel summonOtherPlayerAccess = CommandAccessLevel.OWNERS;

    @JsonIgnore
    public Dialog getConfigurationDialog() {
        Dialog dialog = DialogHelper.getDialog("world_machines.teleport.configure");

        if(dialog.getSections().size() == 4) {
            try {
                dialog.getSections().get(0).getInput().setValue(teleportToPlayerAccess.ordinal());
                dialog.getSections().get(1).getInput().setValue(teleportToPlaqueAccess.ordinal());
                dialog.getSections().get(2).getInput().setValue(teleportInProtectedAreaAccess.ordinal());
                dialog.getSections().get(3).getInput().setValue(summonOtherPlayerAccess.ordinal());
            } catch(Exception e) {}
        }

        return dialog;
    }

    public void configureFromDialog(Player player, Object... ans) {
        if(ans.length < 4) return;
        try {
            CommandAccessLevel[] values = CommandAccessLevel.values();
            teleportToPlayerAccess = values[(int)ans[0]];
            teleportToPlaqueAccess = values[(int)ans[1]];
            teleportInProtectedAreaAccess = values[(int)ans[2]];
            summonOtherPlayerAccess = values[(int)ans[3]];
        } catch(Exception e) {
            player.notify("An unexpected error occurred while configuring the world.");
        }
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
