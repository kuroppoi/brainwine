package brainwine.gameserver.server.requests;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.zone.Zone;

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
            case "player":
                showPlayerDialog(player);
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
    
    private void showPlayerDialog(Player player) {
        // Do nothing if there is no valid input data
        if(input == null || input.length == 0 || !(input[0] instanceof String)) {
            return;
        }
        
        // Create player info dialog
        Player subject = GameServer.getInstance().getPlayerManager().getPlayer((String)input[0]);
        Dialog dialog = new Dialog().setTitle(subject.getName());
        
        // Online status section
        if(subject.isOnline()) {
            dialog.addSection(new DialogSection().setText(String.format("<color=#33AA33>Online</color>\nCurrently in %s", subject.getZone().getName())));
            
            if(player.getZone() != subject.getZone()) {
                if(subject.getZone() == null) {
                    dialog.addSection(new DialogSection().setText("Online but not in a zone."));
                } else {
                    dialog.addSection(new DialogSection().setText(String.format("Goto %s", subject.getZone().getName())).setChoice("visit"));
                }
            }
        } else {
            dialog.addSection(new DialogSection().setText("<color=#C80000>Offline</color>"));
        }
        
        // Follow section
        String followText = player.isFollowing(subject) ? "Unfollow" : "Follow";
        dialog.addSection(new DialogSection().setText(followText).setChoice(followText.toLowerCase()));
        
        // Show player info dialog
        player.showDialog(dialog, input -> {
            // Handle cancellation
            if(input.length == 0 || (input.length == 1 && input[0].equals("cancel"))) {
                return;
            }
            
            String choice = String.valueOf(input[0]);
            
            // Handle selection
            switch(choice) {
            case "follow": player.followPlayer(subject); break;
            case "unfollow": player.unfollowPlayer(subject); break;
            case "visit":
                Zone zone = subject.getZone();
                
                // TODO maybe perform checks in changeZone function and add a force flag for bypassing?
                if(!zone.canJoin(player)) {
                    player.notify("Sorry, you can't enter this world right now.");
                    break;
                }
                
                player.changeZone(subject.getZone());
                break;
            }
        });
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
