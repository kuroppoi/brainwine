package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "admin", description = "Grants or revokes administrator rights.")
public class AdminCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }

        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        } else if(target == executor) {
            executor.notify("You cannot change your own administrator status.", SYSTEM);
            return;
        }
        
        boolean admin = args.length == 1 ? true : Boolean.parseBoolean(args[1]);
        
        if(target.isAdmin() == admin) {
            executor.notify(admin ? "This player is already an administrator." : "This player is not an administrator.", SYSTEM);
            return;
        }
        
        target.setAdmin(admin);
        target.kick(admin ? "You have been given the administrator role! Please restart your game to see its full effects." : "Your administrator privileges have been revoked.");
        executor.notify(String.format("Changed administrator status of player %s to %s", target.getName(), admin), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/admin <player> [true|false]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
