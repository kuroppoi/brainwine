package brainwine.gameserver.command.commands;

import static brainwine.gameserver.command.NotificationType.ALERT;
import static brainwine.gameserver.command.NotificationType.SYSTEM;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class KickCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(player == null) {
            executor.notify("This player does not exist.", ALERT);
            return;
        } else if(!player.isOnline()) {
            executor.notify("This player is offline.", ALERT);
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
    public String getName() {
        return "kick";
    }
    
    @Override
    public String getDescription() {
        return "Kicks a player from the server.";
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
