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

        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.sendMessage("This player does not exist.");
            return;
        } else if(target == executor) {
            executor.sendMessage("You cannot change your own administrator status.");
            return;
        }
        
        boolean admin = args.length == 1 ? true : Boolean.parseBoolean(args[1]);
        target.setAdmin(admin));
        target.kick(admin ? "You have been given the administrator role! Please restart your game to see its full effects." : "Your administrator privileges have been revoked.");
        executor.sendMessage(String.format("Changed administrator status of player %s to %s", target.getName(), admin));
    }
    
    @Override
    public String getName() {
        return "admin";
    }
    
    @Override
    public String getDescription() {
        return "Changes a players administrator status.";
    }
    
    @Override
    public String getUsage() {
        return "/admin <player> [true|false]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
