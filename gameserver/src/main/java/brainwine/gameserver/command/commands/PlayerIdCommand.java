package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class PlayerIdCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player target = null;
        
        if(!(executor instanceof Player)) {
            if(args.length < 1) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
                return;
            }
        } else {
            target = (Player)executor;
        }
                
        if(args.length >= 1) {
            target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        executor.notify(target.getDocumentId(), SYSTEM);
    }
    
    @Override
    public String getName() {
        return "pid";
    }
    
    @Override
    public String getDescription() {
        return "Displays the document id of a player.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/pid %s", executor instanceof Player ? "[player]" : "<player>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
