package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "time", description = "Displays or changes the time in the current zone.")
public class TimeCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone zone = ((Player)executor).getZone();
        
        if(args.length < 1) {
            executor.notify(String.format("The current time in %s is %s", zone.getName(), zone.getTime()), SYSTEM);
            return;
        }
        
        String time = args[0].toLowerCase();
        float value = 0.0f;
        
        switch(time) {
            case "day": value = 0.5f; break;
            case "night": value = 0.0f; break;
            case "dawn": value = 0.25f; break;
            case "dusk": value = 0.75f; break;
            default:
                try {
                    value = Float.parseFloat(time);
                } catch(NumberFormatException e) {
                    executor.notify("Time must be day, night, dawn, dusk or a number between 0.0 and 1.0", SYSTEM);
                    return;
                }
                
                if(value < 0.0f || value > 1.0f) {
                    executor.notify("Time must be a number between 0.0 and 1.0", SYSTEM);
                    return;
                }
                
                break;
        }
                
        zone.setTime(value);
        executor.notify(String.format("Time has been set to %s in %s.", value, zone.getName()), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/time [value|(day|night|dawn|dusk)]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
