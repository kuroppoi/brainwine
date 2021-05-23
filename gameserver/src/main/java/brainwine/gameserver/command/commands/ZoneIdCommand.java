package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;
import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.Zone;

public class ZoneIdCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone target = null;
        
        if(!(executor instanceof Player)) {
            if(args.length < 1) {
                executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
                return;
            }
        } else {
            target = ((Player)executor).getZone();
        }
        
        if(args.length >= 1) {
            target = GameServer.getInstance().getZoneManager().getZoneByName(String.join(" ", args));
        }
        
        if(target == null) {
            executor.notify("This zone does not exist.", ALERT);
            return;
        }
        
        executor.notify(target.getDocumentId(), SYSTEM);
    }
    
    @Override
    public String getName() {
        return "zid";
    }
    
    @Override
    public String getDescription() {
        return "Displays the document id of a zone.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/zid %s", executor instanceof Player ? "[zone]" : "<zone>");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
