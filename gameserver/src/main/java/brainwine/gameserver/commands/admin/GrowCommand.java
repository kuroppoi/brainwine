package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
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