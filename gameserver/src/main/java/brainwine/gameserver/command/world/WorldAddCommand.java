package brainwine.gameserver.command.world;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wadd", description = "Add a member to your private world.")
public class WorldAddCommand extends WorldCommand {

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
        
        // World owners cannot add themselves as members
        if(zone.isOwner(target)) {
            player.notify("You own this world and cannot add yourself as a member.");
            return;
        }
        
        // Check if target is already a member
        if(zone.isMember(target)) {
            player.notify(String.format("%s is already a member of this world.", target.getName()));
            return;
        }
        
        // TODO send feedback to target
        zone.addMember(target);
        player.notify(String.format("%s has been added.", target.getName()));
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wadd <player>";
    }
}
