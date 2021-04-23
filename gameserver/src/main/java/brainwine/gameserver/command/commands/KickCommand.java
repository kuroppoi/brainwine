package brainwine.gameserver.command.commands;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class KickCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.sendMessage(String.format("Usage: %s", getUsage()));
            return;
        }
        
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(player == null) {
            executor.sendMessage("This player does not exist.");
            return;
        } else if(!player.isOnline()) {
            executor.sendMessage("This player is offline.");
            return;
        }
        
        String reason = "You have been kicked from the server.";
        
        if(args.length > 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }
        
        player.kick(reason);
        executor.sendMessage("Kicked player " + player.getName() + " for '" + reason + "'");
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
    public String getUsage() {
        return "/kick <player> [reason]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
