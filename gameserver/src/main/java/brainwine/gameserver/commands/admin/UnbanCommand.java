package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "unban", description = "Unbans a player.", aliases = "pardon")
public class UnbanCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        if(!target.isBanned()) {
            executor.notify(String.format("%s isn't currently banned.", target.getName()), SYSTEM);
            return;
        }
        
        target.unban(executor instanceof Player ? (Player)executor : null);
        executor.notify(String.format("Player %s has been unbanned.", target.getName()), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/unban <player>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
