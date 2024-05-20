package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "level", description = "Sets the level of the target player.", aliases = { "lvl", "lv" })
public class LevelCommand extends Command {
    
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
        
        int level = 0;
        
        try {
            level = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Level must be a valid number.", SYSTEM);
            return;
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        int maxLevel = target.getMaxLevel();
        
        if(level < 1 || level > maxLevel) {
            executor.notify(String.format("Level must be between 1 and %s.", maxLevel), SYSTEM);
            return;
        }
        
        target.setLevel(level);
        executor.notify(String.format("Successfully set %s's level to %s.", target.getName(), level), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/level <level> %s", executor instanceof Player ? "[player]" : "<player>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
