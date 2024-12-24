package brainwine.gameserver.command.world;

import java.util.List;
import java.util.stream.Collectors;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "winfo", description = "Displays private world information.")
public class WorldInfoCommand extends WorldCommand {
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        // Fetch member names
        List<String> memberNames = zone.getMembers().stream()
                .map(x -> GameServer.getInstance().getPlayerManager().getPlayerById(x).getName())
                .collect(Collectors.toList());
        
        // Create & show world info dialog
        // TODO there's gotta be a cleaner way to do this...
        Dialog dialog = new Dialog()
            .addSection(new DialogSection().setTitle("World Info"))
            .addSection(new DialogSection().setText(" "))
            .addSection(new DialogSection().setText(player.isV3() ? "<color=#4d5b82>Entry Code</color>" : "Entry Code").setTextColor("4d5b82"))
            .addSection(new DialogSection().setText("Entry codes are currently unavailable."))
            .addSection(new DialogSection().setText(" "))
            .addSection(new DialogSection().setText(player.isV3() ? "<color=#4d5b82>Members</color>" : "Members").setTextColor("4d5b82"))
            .addSection(new DialogSection().setText(memberNames.isEmpty() ? "None :(" : memberNames.toString().replaceAll("[\\[+\\]]", "")));
        player.showDialog(dialog);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/winfo";
    }
}
