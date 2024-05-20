package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "skillpoints", description = "Sets the skill points of the target player.", aliases = "points")
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
        target.notify(String.format("Your skill point count has been set to %s.", amount), SYSTEM);
        executor.notify(String.format("Successfully set %s's skill point count to %s.", target.getName(), amount), SYSTEM);
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
