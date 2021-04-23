package brainwine.gameserver.command.commands;

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
                executor.sendMessage("Usage: /zid <zone>");
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
        
        executor.sendMessage(target.getDocumentId());
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
    public String getUsage() {
        return "/zid [zone]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
