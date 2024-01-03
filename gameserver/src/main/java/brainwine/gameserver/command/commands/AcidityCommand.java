package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.Zone;

public class AcidityCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone zone = ((Player)executor).getZone();
        
        if(args.length < 1) {
            executor.notify(String.format("The current acidity of %s is %s", zone.getName(), zone.getAcidity()), SYSTEM);
            return;
        }
        
        float value = 0.0f;
        
        try {
            value = Float.parseFloat(args[0]);
        } catch(NumberFormatException e) {
            executor.notify("Acidity must be a number between 0.0 and 1.0", SYSTEM);
            return;
        }
        
        if(value < 0.0f || value > 1.0f) {
            executor.notify("Acidity must be a number between 0.0 and 1.0", SYSTEM);
            return;
        }
                
        zone.setAcidity(value);
        executor.notify(String.format("Acidity has been set to %s in %s.", value, zone.getName()), SYSTEM);
    }

    @Override
    public String getName() {
        return "acidity";
    }
    
    @Override
    public String getDescription() {
        return "Displays or changes the acidity in the current zone.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/acidity [value]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
