package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "whelp", description = "Displays the world help menu.")
public class WorldHelpCommand extends WorldCommand {
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        player.showDialog(DialogHelper.getDialog("world_help"));
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/whelp";
    }
}
