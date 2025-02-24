package brainwine.gameserver.zone;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassSpawnerConfiguration {
    int difficulty = 3;
    boolean mawSpawningEnabled = true;
    boolean areaSpawningEnabled = true;
    CommandAccessLevel evokeAccess = CommandAccessLevel.OWNERS;

    @JsonIgnore
    public Dialog getConfigurationDialog(final float availablePower) {
        Dialog dialog = DialogHelper.getDialog("world_machines.spawner.configure");

        List<Map<String, Object>> sections = MapHelper.getList(GameConfiguration.getBaseConfig(), "dialogs.world_machines.spawner.configure.sections");
        List<DialogSection> retainedSections = sections.stream()
                .filter(s -> (s.get("power") == null || (int) s.get("power") <= availablePower))
                .map(s -> {
                    try {
                        return JsonHelper.readValue(s, DialogSection.class);
                    } catch(JsonProcessingException e) {
                        return new DialogSection().setTitle("ERROR").setInput(new DialogTextIndexInput().setValue(0));
                    }
                })
                .collect(Collectors.toList());

        dialog.getSections().clear();
        dialog.getSections().addAll(retainedSections);

        int n = dialog.getSections().size();
        if(n >= 1) dialog.getSections().get(0).getInput().setValue(difficulty / 2);
        if(n >= 2) dialog.getSections().get(1).getInput().setValue(mawSpawningEnabled ? 0 : 1);
        if(n >= 3) dialog.getSections().get(2).getInput().setValue(areaSpawningEnabled ? 0 : 1);
        if(n >= 4) dialog.getSections().get(3).getInput().setValue(evokeAccess.ordinal());

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
        System.out.println(Arrays.asList(ans));
        // NOTE: This depends on the non-decreasing order of input powers i.e. there are no skipped sections in between.
        try {
            difficulty = ans.length >= 1 ? MathUtils.clamp(2 * (int)ans[0] + 1, 1, 5) : 3;
            mawSpawningEnabled = ans.length >= 2 ? (int)ans[1] == 0 : true;
            areaSpawningEnabled = ans.length >= 3 ? (int)ans[2] == 0 : true;
            evokeAccess = ans.length >= 4 ? CommandAccessLevel.values()[(int)ans[3]] : CommandAccessLevel.OWNERS;
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
