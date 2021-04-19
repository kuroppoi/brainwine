package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class BroadcastCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.alert("Usage: /broadcast <message>");
            return;
        }
        
        String text = String.join(" ", args);
        
        // TODO hook the console up to some kind of chat feed?
        if(executor instanceof GameServer) {
            executor.alert(text);
        }
        
        for(Player player : GameServer.getInstance().getPlayerManager().getPlayers()) {
            player.notify(text, 9);
        }
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
