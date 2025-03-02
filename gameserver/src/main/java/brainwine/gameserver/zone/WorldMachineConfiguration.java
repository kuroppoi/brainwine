package brainwine.gameserver.zone;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class WorldMachineConfiguration {
    protected abstract String getDialogName();
    protected abstract void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException;
    protected abstract Object getValue(String key);

    public void configure(Player player, Zone zone, Item item) {
        Dialog dialog = getConfigurationDialog(item.getPower());
        if(dialog == null) {
            player.notify("Configuration dialog not found!");
            return;
        }
        player.showDialog(dialog, ans -> handleConfigurationDialog(player, zone, dialog, ans));
    }

    private Dialog getConfigurationDialog(float availablePower) {
        Map<String, Object> original = MapHelper.getMap(GameConfiguration.getBaseConfig(), getDialogName());
        if(original == null) return null;
        Map<String, Object> config = new HashMap<>(original);
        List<Map<String, Object>> sections = (List<Map<String, Object>>)config.get("sections");

        List<DialogSection> retainedSections = sections.stream()
                .filter(s -> (s.get("power") == null || (int) s.get("power") <= availablePower))
                .map(s -> {
                    DialogSection section;
                    try {
                        section = JsonHelper.readValue(s, DialogSection.class);
                    } catch(JsonProcessingException e) {
                        section = new DialogSection().setTitle("ERROR").setInput(new DialogTextIndexInput());
                    }

                    Object value = getValue(section.getInput().getKey());
                    if(value != null) {
                        section.getInput().setValue(value);
                    }

                    return section;
                })
                .collect(Collectors.toList());
        config.put("sections", retainedSections);

        try {
            return JsonHelper.readValue(config, Dialog.class);
        } catch(JsonProcessingException e) {
            return null;
        }
    }

    private void handleConfigurationDialog(Player player, Zone zone, Dialog dialog, Object[] ans) {
        if(player.getZone() == null || (!player.isGodMode() && !player.getZone().isOwner(player))) {
            player.notify("Sorry, you do not own this world.");
            return;
        }

        if(dialog.getSections().size() > ans.length) return;

        Map<String, Object> values = new HashMap<>();

        for(int i = 0; i < dialog.getSections().size(); i++) {
            if(ans[i] != null) {
                values.put(dialog.getSections().get(i).getInput().getKey(), ans[i]);
            } else {
                player.notify("Invalid input!");
            }
        }

        try {
            configure(zone, values);
        } catch(IllegalArgumentException e) {
            player.notify("Invalid input!");
        }
    }

}
