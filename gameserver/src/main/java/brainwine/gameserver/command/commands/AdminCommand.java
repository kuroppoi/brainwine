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
        
        player.setAdmin(Boolean.parseBoolean(args.length == 2 ? args[1] : "true"));
        player.kick("Updated User Status\n\n" + "Admin: " + player.getAdmin());
        executor.sendMessage("Changed Admin status of user " + player.getName() + " to " + player.getAdmin());
    }
    
    @Override
    public String getName() {
        return "admin";
    }
    
    @Override
    public String getDescription() {
        return "Sets a user's Administrator status.";
    }
    
    @Override
    public String getUsage() {
        return "/admin <player> <true/false>";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
