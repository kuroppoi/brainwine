package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "seed", description = "Displays the seed of a zone.")
public class SeedCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone target = null;
        
        if(!(executor instanceof Player)) {
            if(args.length < 1) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
                return;
            }
        } else {
            target = ((Player)executor).getZone();
        }
        
        if(args.length >= 1) {
            target = GameServer.getInstance().getZoneManager().getZoneByName(String.join(" ", args));
        }
        
        if(target == null) {
            executor.notify("This zone does not exist.", SYSTEM);
            return;
        }
        
        executor.notify("Seed: " + target.getSeed(), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/seed %s", executor instanceof Player ? "[zone]" : "<zone>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}