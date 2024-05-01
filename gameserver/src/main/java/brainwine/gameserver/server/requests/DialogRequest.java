package brainwine.gameserver.server.requests;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 45)
public class DialogRequest extends PlayerRequest {
    
    public Object id;
    
    @OptionalField
    public Object[] input;
    
    @Override
    public void process(Player player) {
        if(input != null && input.length == 1 && input[0] instanceof Map) {
            input = ((Map<?, ?>)input[0]).values().toArray();
        }
        
        if(id instanceof String) {
            switch((String)id) {
                case "skill_upgrade":
                    onSkillUpgrade(player);
                    break;
                default:
                    player.notify("Sorry, this action is not implemented yet.");
                    break;
            }
            return;
        } else if(id instanceof Integer) {
            if(input != null && (int)id > 0) {
                player.handleDialogInput((int)id, input);
            }
        }
    }
    
    private void onSkillUpgrade(Player player) {
        if(player.getSkillPoints() <= 0) {
            player.notify("Sorry, you are out of skill points. Level up to earn some more!");
            return;
        }
        
        Collection<Skill> upgradeableSkills = player.getUpgradeableSkills();
        List<String> upgradeableSkillNames = upgradeableSkills.stream()
                .map(Skill::getId)
                .map(WordUtils::capitalize)
                .collect(Collectors.toList());
        
        if(upgradeableSkills.isEmpty()) {
            player.notify("You've maxed out all available skills!");
            return;
        }
        
        // I love programmable dialogs!!!
        Dialog dialog = new Dialog()
                .addSection(new DialogSection()
                        .setTitle("Choose a skill to upgrade:")
                        .setText(player.getLevel() < 10 ?
                                "Note: Additional skills like Combat and Engineering are unlocked as you progress." : null)
                        .setInput(new DialogSelectInput()
                                .setOptions(upgradeableSkillNames)
                                .setMaxColumns(3)
                                .setKey("skill")));
        
        player.showDialog(dialog, input -> {
            if(input.length == 0 || input[0].equals("cancel")) {
                return;
            }
            
            if(player.getSkillPoints() <= 0) {
                player.notify("Sorry, you are out of skill points. Level up to earn some more!");
                return;
            }
            
            Skill skill = Skill.fromId(input[0].toString());
            
            if(!player.getUpgradeableSkills().contains(skill)) {
                player.notify("Sorry, you cannot upgrade that skill right now.");
                return;
            }
            
            int newSkillLevel = player.getSkillLevel(skill) + 1;
            player.setSkillLevel(skill, newSkillLevel);
            player.setSkillPoints(player.getSkillPoints() - 1);
            player.showDialog(DialogHelper.messageDialog(String.format("You've successfully upgraded your %s skill to level %s!",
            WordUtils.capitalize(skill.getId()), newSkillLevel)));
        });
    }
}
