package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "kick", description = "Kicks a player from the server.")
public class KickCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(player == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        } else if(!player.isOnline()) {
            executor.notify("This player is offline.", SYSTEM);
            return;
        }
        
        String reason = "You have been kicked from the server.";
        
        if(args.length > 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }
        
        player.kick(reason);
        executor.notify("Kicked player " + player.getName() + " for '" + reason + "'", SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/kick <player> [reason]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
