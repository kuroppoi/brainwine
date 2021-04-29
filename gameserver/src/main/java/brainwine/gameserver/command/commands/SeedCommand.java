package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.Zone;

public class SeedCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone target = null;
        
        if(!(executor instanceof Player)) {
            if(args.length < 1) {
                executor.sendMessage("Usage: /seed <zone>");
                return;
            }
        } else {
            target = ((Player)executor).getZone();
        }
        
        if(args.length >= 1) {
            target = GameServer.getInstance().getZoneManager().getZoneByName(String.join(" ", args));
        }
        
        if(target == null) {
            executor.sendMessage("This zone does not exist.");
            return;
        }
        
        executor.sendMessage("Seed: " + target.getSeed());
    }
    
    @Override
    public String getName() {
        return "seed";
    }
    
    @Override
    public String getDescription() {
        return "Displays the seed of a zone.";
    }
    
    @Override
    public String getUsage() {
        return "/seed [zone]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}