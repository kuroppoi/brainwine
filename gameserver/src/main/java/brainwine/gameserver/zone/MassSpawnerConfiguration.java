package brainwine.gameserver.zone;

import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.util.MathUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassSpawnerConfiguration {
    int difficulty = 3;
    boolean mawSpawningEnabled = true;
    boolean areaSpawningEnabled = true;
    CommandAccessLevel evokeAccess = CommandAccessLevel.OWNERS;

    @JsonIgnore
    public Dialog getConfigurationDialog() {
        Dialog dialog = DialogHelper.getDialog("world_machines.spawner.configure");

        if(dialog.getSections().size() == 4) {
            try {
                dialog.getSections().get(0).getInput().setValue(difficulty / 2);
                dialog.getSections().get(1).getInput().setValue(mawSpawningEnabled ? 0 : 1);
                dialog.getSections().get(2).getInput().setValue(areaSpawningEnabled ? 0 : 1);
                dialog.getSections().get(3).getInput().setValue(evokeAccess.ordinal());
            } catch(Exception e) {}
        }

        return dialog;
    }

    public void reset(Zone zone) {
        difficulty = 3;
        mawSpawningEnabled = true;
        areaSpawningEnabled = true;
        evokeAccess = CommandAccessLevel.OWNERS;
        onUpdate(zone);
    }

    public void configureFromDialog(Player player, Object... ans) {
        if(ans.length < 4) return;
        try {
            difficulty = MathUtils.clamp(2 * (int)ans[0] + 1, 1, 5);
            mawSpawningEnabled = (int)ans[1] == 0;
            areaSpawningEnabled = (int)ans[2] == 0;
            evokeAccess = CommandAccessLevel.values()[(int)ans[3]];
        } catch(Exception e) {
            player.notify("An unexpected error occurred while configuring the world.");
        }
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
