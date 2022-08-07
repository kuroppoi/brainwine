package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class ExperienceCommand extends Command {
    
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
        
        int experience = 0;
        
        try {
            experience = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Experience must be a valid number.", SYSTEM);
            return;
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        if(experience < 0) {
            executor.notify("Experience must be greater than 0.", SYSTEM);
            return;
        }
        
        target.setExperience(experience);
        executor.notify(String.format("Successfully set %s's experience to %s.", target.getName(), experience), SYSTEM);
    }

    @Override
    public String getName() {
        return "experience";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "xp" };
    }
    
    @Override
    public String getDescription() {
        return "Sets the experience of the target player.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/experience <experience> %s", executor instanceof Player ? "[player]" : "<player>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}