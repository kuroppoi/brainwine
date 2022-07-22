package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class HealthCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player target = null;
        
        if(args.length < 2) {
            if(args.length == 0 || !(executor instanceof Player)) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
                return;
            }
            target = (Player)executor;
        } else {
            target = GameServer.getInstance().getPlayerManager().getPlayer(args[1]);
        }
        
        if(target == null) {
            executor.notify("This player does not exist.", ALERT);
            return;
        } else if(!target.isOnline()) {
            executor.notify("This player is offline.", ALERT);
            return;
        }
        
        float health = 0;
        
        try {
            health = Float.parseFloat(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Health must be a valid number.", ALERT);
            return;
        }
        
        target.setHealth(health);
        target.alert(String.format("Your health has been set to %s", target.getHealth()));
        
        if(target != executor) {
            executor.notify(String.format("Set %s's health to %s", target.getName(), target.getHealth()), ALERT);
        }
    }

    @Override
    public String getName() {
        return "health";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "hp" };
    }
    
    @Override
    public String getDescription() {
        return "Sets a player's health.";
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
