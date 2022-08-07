package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class SkillPointsCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player target = null;
        
        if(!(executor instanceof Player)) {
            if(args.length < 2) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
                return;
            }
        } else {
            if(args.length < 1) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
                return;
            } else {
                target = (Player)executor;
            }
        }
        
        if(args.length >= 2) {
            target = GameServer.getInstance().getPlayerManager().getPlayer(args[1]);
        }
        
        int amount = 0;
        
        try {
            amount = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Amount must be a valid number.", SYSTEM);
            return;
        }
        
        if(amount < 0) {
            executor.notify("Amount must be positive.", SYSTEM);
            return;
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        target.setSkillPoints(amount);
        target.alert(String.format("Your skill point count has been set to %s.", amount));
        executor.notify(String.format("Successfully set %s's skill point count to %s.", target.getName(), amount), SYSTEM);
    }

    @Override
    public String getName() {
        return "skillpoints";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "points" };
    }
    
    @Override
    public String getDescription() {
        return "Sets the skill points of the target player.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/skillpoints <amount> %s", executor instanceof Player ? "[player]" : "<player>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
