package brainwine.gameserver.item.consumables;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.messages.InventoryMessage;

/**
 * Consumable handler for skill upgrade items
 */
public class SkillConsumable implements Consumable {
    
    @Override
    public void consume(Item item, Player player, Object details) {
        List<Skill> bumpedSkills = player.getBumpedSkills().getOrDefault(item, Collections.emptyList());
        
        // Check if all skills have been bumped already
        if(bumpedSkills.size() >= Skill.values().length) {
            player.notify(String.format("You have already increased all of your skills with %ss.", item.getTitle().toLowerCase()));
            player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        // Assemble a list of skills that can be upgraded with this consumable
        List<Skill> upgradeableSkills = Arrays.asList(Skill.values()).stream()
                .filter(skill -> !bumpedSkills.contains(skill) && player.getSkillLevel(skill) < 10)
                .collect(Collectors.toList());
        
        // Check if there are any skills to upgrade
        if(upgradeableSkills.isEmpty()) {
            player.notify("You have maximized all skills available for mastery.");
            player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        List<String> upgradeableSkillNames = upgradeableSkills.stream()
                .map(Skill::getId)
                .map(WordUtils::capitalize)
                .collect(Collectors.toList());
        
        // Create dialog
        Dialog dialog = new Dialog().addSection(new DialogSection()
                .setTitle("Which skill would you like to increase?")
                .setInput(new DialogSelectInput()
                        .setOptions(upgradeableSkillNames)
                        .setKey("skill")));
        
        player.showDialog(dialog, data -> {
            // Verify data
            if(data.length != 1) {
                fail(item, player);
                return;
            }
            
            Skill skill = Skill.fromId("" + data[0]);
            
            // Make sure that the skill is still eligible for upgrading
            if(player.hasSkillBeenBumped(item, skill) || player.getSkillLevel(skill) >= 10) {
                fail(item, player);
                return;
            }
            
            player.getInventory().removeItem(item, true); // Remove consumable
            player.trackSkillBump(item, skill); // Track skill bump
            player.setSkillLevel(skill, player.getSkillLevel(skill) + 1); // Increase skill level
            player.showDialog(DialogHelper.messageDialog(String.format("%s increased!", WordUtils.capitalize(skill.toString().toLowerCase())),
                    String.format("You now have additional mastery of %s.", skill.toString().toLowerCase())));
        });
    }
    
    private void fail(Item item, Player player) {
        player.notify("Oops! There was a problem with upgrading your skill.");
        player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
