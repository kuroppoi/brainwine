package brainwine.gameserver.command;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;

public class KickCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.alert("Usage: /kick <player> [reason]");
            return;
        }
        
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(player == null) {
            executor.alert("This player does not exist.");
            return;
        } else if(!player.isOnline()) {
            executor.alert("This player is offline.");
            return;
        }
        
        String reason = "You have been kicked from the server.";
        
        if(args.length > 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }
        
        player.kick(reason);
        executor.alert("Kicked player " + player.getName() + " for '" + reason + "'");
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
