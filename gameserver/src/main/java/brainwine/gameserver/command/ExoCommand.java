package brainwine.gameserver.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.AppearanceSlot;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "exo", description = "Configure exosuit visibility settings.")
public class ExoCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;
        
        // TODO: text index would be far more appropriate for this
        Map<String, Integer> headgearKeys = new HashMap<>();
        Map<String, Integer> torsoKeys = new HashMap<>();
        Map<String, Integer> legsKeys = new HashMap<>();
        Dialog dialog = new Dialog()
                .addSection(new DialogSection().setTitle("Exo Visibility"))
                .addSection(createSlotSection(player, headgearKeys, AppearanceSlot.FACIAL_GEAR, "Headset", "headset"))
                .addSection(createSlotSection(player, torsoKeys, AppearanceSlot.TOPS_OVERLAY, "Torso", "torso"))
                .addSection(createSlotSection(player, legsKeys, AppearanceSlot.LEGS_OVERLAY, "Legs", "legs"));
        
       player.showDialog(dialog, data -> {
           // Handle cancellation
           if(data.length == 1 && data[0].equals("cancel")) {
               return;
           }
           
           // Check data length
           if(data.length != 3) {
               return;
           }
           
           // Update player appearance
           // TODO: Hiding exoskeletons is not implemented properly on v3 clients.
           // Players will need to relog in order for changes to apply properly.
           Map<String, Object> appearance = new HashMap<>();
           appearance.put(AppearanceSlot.FACIAL_GEAR.getId(), headgearKeys.getOrDefault(String.valueOf(data[0]), 0));
           appearance.put(AppearanceSlot.TOPS_OVERLAY.getId(), torsoKeys.getOrDefault(String.valueOf(data[1]), 0));
           appearance.put(AppearanceSlot.LEGS_OVERLAY.getId(), legsKeys.getOrDefault(String.valueOf(data[2]), 0));
           player.updateAppearance(appearance);
       });
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/exo";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
    
    private static DialogSection createSlotSection(Player player, Map<String, Integer> keyMap, AppearanceSlot slot, String text, String key) {
        List<String> options = new ArrayList<>();
        
        for(Item item : player.getInventory().getAccessories()) {
            if(item.getAppearanceSlot() == slot) {
                String option = item.getTitle().split(" ", 2)[0];
                options.add(option);
                keyMap.put(option, item.getCode());
            }
        }
        
        // TODO options are sorted inversely by item code but should ideally be sorted by some kind of arbitrary tier
        options.sort((a, b) -> Integer.compare(keyMap.get(b), keyMap.get(a)));
        options.add("Hidden");
        return new DialogSection().setText(text).setInput(new DialogSelectInput().setOptions(options).setKey(key));
    }
}
