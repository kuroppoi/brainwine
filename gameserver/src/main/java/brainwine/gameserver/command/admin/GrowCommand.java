package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.GrowthManager;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "grow", description = "Simulate plant growth in all loaded chunks.")
public class GrowCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        int cycles = 0;
        
        try {
            cycles = Math.min(GrowthManager.MAX_RAIN_CYCLES, Integer.parseInt(args[0]));
        } catch(NumberFormatException e) {
            executor.notify("Rain cycles must be a valid number.", SYSTEM);
            return;
        }
        
        Player player = (Player)executor;
        Zone zone = player.getZone();
        zone.updateGrowables(cycles);
        player.notify(String.format("Simulated %s rain cycles.", cycles), SYSTEM);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/grow <cycles>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
