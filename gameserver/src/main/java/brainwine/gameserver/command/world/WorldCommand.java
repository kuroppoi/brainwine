package brainwine.gameserver.command.world;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

/**
 * Base class for commands regarding private world management.
 */
public abstract class WorldCommand extends Command {
    
    public abstract void execute(Zone zone, Player player, String[] args);
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;
        Zone zone = player.getZone();
        
        // Check if player owns world
        if(!player.isGodMode() && !zone.isOwner(player)) {
            player.notify("Sorry, you do not own this world.");
            return;
        }
        
        execute(zone, player, args);
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
