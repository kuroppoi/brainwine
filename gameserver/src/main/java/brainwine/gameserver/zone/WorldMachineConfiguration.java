package brainwine.gameserver.zone;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class WorldMachineConfiguration {
    @JsonIgnore
    protected Zone zone;
    private int machineX;
    private int machineY;

    protected abstract String getDialogName();
    protected abstract void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException;

    protected abstract Object getValue(String key);

    protected boolean isEnabled(String itemName) {
        Block block = zone.getBlock(machineX, machineY);
        if(block == null || !block.getFrontItem().getId().startsWith(itemName)) return false;
        return block.getFrontMod() > 0;
    }

    protected boolean isEnabled() {
        return false;
    }

    public <T extends WorldMachineConfiguration> T setZone(Zone zone) {
        this.zone = zone;
        return (T)this;
    }

    public int getMachineX() {
        return machineX;
    }

    public int getMachineY() {
        return machineY;
    }

    protected String getPublicDialogName() {
        return "override.me";
    }

    public void handleCommand(Player player, Zone zone, Item item, String command) {}

    public void configure(Player player, Zone zone, Item item, int x, int y) {
        configure(player, zone, item.getPower(), x, y);
    }

    public void configure(Player player, Zone zone, float availablePower, int x, int y) {
        Dialog dialog = getConfigurationDialog(availablePower);

        if(dialog == null) {
            player.notify("Configuration dialog not found!");
            return;
        }

        player.showDialog(dialog, ans -> handleConfigurationDialog(player, zone, dialog, ans, x, y));
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

    private void handleConfigurationDialog(Player player, Zone zone, Dialog dialog, Object[] ans, int x, int y) {
        if(ans.length >= 1 && ans[0].equals("cancel")) return;

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
            if(x != -1 && y != -1) {
                machineX = x;
                machineY = y;
            }
        } catch(IllegalArgumentException e) {
            player.notify("Invalid input!");
        }
    }

    private void handlePublicDialog(Player player, Zone zone, Item item, Dialog dialog, Object[] ans) {
        if(ans.length < 1) return;
        if(ans.length > 1) {
            player.notify("I don't know what to do.");
            return;
        }
        if("cancel".equals(ans[0])) return;
        if(ans[0] instanceof String) {
            handleCommand(player, zone, item, (String)ans[0]);
        }
    }

    public void interactPublicly(Player player, Zone zone, Item item) {
        Dialog dialog = null;

        // Filter out that require too much power.
        try {
            float availablePower = item.getPower();
            Map<String, Object> dialogConfig = new HashMap<>(MapHelper.getMap(GameConfiguration.getBaseConfig(), getPublicDialogName()));
            if(dialogConfig.containsKey("sections")) {
                List<Object> retainedSections = new ArrayList<>();
                for(Object section : MapHelper.getList(dialogConfig, "sections")) {
                    if(section instanceof Map) {
                        Object power = ((Map<String, Object>) section).get("power");
                        if(power == null || (int) power <= availablePower) {
                            retainedSections.add(section);
                        }
                    }
                }
                dialogConfig.put("sections", retainedSections);
                dialog = JsonHelper.readValue(dialogConfig, Dialog.class);
            }
        } catch(Exception e) {
            e.printStackTrace();
            player.showDialog(DialogHelper.messageDialog("Public Dialog Parsing Error", e.getMessage()));
            return;
        }

        final Dialog finalDialog = dialog;
        player.showDialog(dialog, ans -> handlePublicDialog(player, zone, item, finalDialog, ans));
    }

    protected String expectString(Object obj) throws IllegalArgumentException {
        if(obj instanceof String) {
            return (String)obj;
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected int expectInteger(Object obj) throws IllegalArgumentException {
        if(obj instanceof Integer) {
            return (int)obj;
        }

        if(obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch(Exception ignored) {}
        }

        throw new IllegalArgumentException();
    }

}
