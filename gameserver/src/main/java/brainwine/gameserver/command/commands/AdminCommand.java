package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class AdminCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
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
        
        player.setAdmin(!player.getAdmin());
        executor.sendMessage("Kicked player " + player.getName());
    }
    
    @Override
    public String getName() {
        return "admin";
    }
    
    @Override
    public String getDescription() {
        return "Toggles a user's Administrator status.";
    }
    
    @Override
    public String getUsage() {
        return "/admin <player>";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
