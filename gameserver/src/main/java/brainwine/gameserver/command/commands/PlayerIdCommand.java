package brainwine.gameserver.command.commands;

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
                executor.sendMessage("Usage: /pid <player>");
                return;
            }
        } else {
            target = (Player)executor;
        }
                
        if(args.length >= 1) {
            target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        }
        
        if(target == null) {
            executor.sendMessage("This player does not exist.");
            return;
        }
        
        executor.sendMessage(target.getDocumentId());
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
    public String getUsage() {
        return "/pid [player]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
