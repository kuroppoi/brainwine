package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "acidity", description = "Displays or changes the acidity in the current zone.")
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
    public String getUsage(CommandExecutor executor) {
        return "/acidity [value]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
