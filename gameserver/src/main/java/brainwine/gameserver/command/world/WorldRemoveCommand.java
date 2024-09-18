package brainwine.gameserver.command.world;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wremove", description = "Remove a member from your private world.")
public class WorldRemoveCommand extends WorldCommand {
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        // Check if target exists
        if(target == null) {
            player.notify(String.format("Player '%s' not found.", args[0]));
            return;
        }
        
        // Check if target is not a member
        if(!zone.isMember(target)) {
            player.notify(String.format("%s is not a member of this world.", target.getName()));
            return;
        }
        
        zone.removeMember(target);
        player.notify(String.format("%s has been removed.", target.getName()));
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wremove <player>";
    }
}
