package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "health", description = "Sets the target player's health.", aliases = "hp")
public class HealthCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player target = null;
        
        if(args.length < 2) {
            if(args.length == 0 || !(executor instanceof Player)) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
                return;
            }
            target = (Player)executor;
        } else {
            target = GameServer.getInstance().getPlayerManager().getPlayer(args[1]);
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        } else if(!target.isOnline()) {
            executor.notify("This player is offline.", SYSTEM);
            return;
        }
        
        float health = 0;
        
        try {
            health = Float.parseFloat(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Health must be a valid number.", SYSTEM);
            return;
        }
        
        target.setHealth(health);
        target.notify(String.format("Your health has been set to %s", target.getHealth()), SYSTEM);
        
        if(target != executor) {
            executor.notify(String.format("Set %s's health to %s", target.getName(), target.getHealth()), SYSTEM);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/health <amount> %s", executor instanceof Player ? "[player]" : "<player>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
