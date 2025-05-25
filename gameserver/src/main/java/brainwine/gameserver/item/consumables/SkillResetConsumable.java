package brainwine.gameserver.item.consumables;

import java.util.Map.Entry;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.server.messages.InventoryMessage;

/**
 * Consumable handler for skill resets
 */
public class SkillResetConsumable implements Consumable {
    
    @Override
    public void consume(Item item, Player player, Object details) {
        // Create dialog
        Dialog dialog = new Dialog()
                .setActions("yesno")
                .addSection(new DialogSection()
                        .setTitle("Confirm skill reset")
                        .setText("Are you sure that you want to reset all of your skills back to level 1?"));
                
        player.showDialog(dialog, data -> {
            // Handle cancellation
            if(data.length == 1 && data[0].equals("cancel")) {
                player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
                return;
            }
            
            // Check if there are any skills to reset
            if(!player.getSkills().values().stream().anyMatch(level -> level > 1)) {
                player.showDialog(DialogHelper.messageDialog("You don't have any skills to reset."));
                player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
                return;
            }
            
            int pointsToRefund = 0;
            
            // Reset skill levels and calculate point refund total
            for(Entry<Skill, Integer> entry : player.getSkills().entrySet()) {
                Skill skill = entry.getKey();
                int level = entry.getValue();
                
                // Skip if skill hasn't been upgraded at all
                if(level <= 1) {
                    continue;
                }
                
                pointsToRefund += level - 1;
                player.setSkillLevel(skill, 1); // Reset skill level
            }
            
            player.getInventory().removeItem(item, true); // Remove the consumable
            player.setSkillPoints(player.getSkillPoints() + pointsToRefund); // Refund skill points
            player.showDialog(DialogHelper.getDialog("skill_reset"));
        });
    }
}
