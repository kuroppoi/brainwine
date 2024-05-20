package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.POPUP;
import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "broadcast", description = "Broadcasts a message to all online players.", aliases = "bc")
public class BroadcastCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        String text = "[Server Notification] " + String.join(" ", args);
        
        for(Player player : GameServer.getInstance().getPlayerManager().getPlayers()) {
            player.notify(text, SYSTEM);
        }
        
        executor.notify("Your message has been broadcasted.", POPUP);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/broadcast <message>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
