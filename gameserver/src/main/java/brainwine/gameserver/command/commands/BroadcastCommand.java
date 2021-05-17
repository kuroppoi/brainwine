package brainwine.gameserver.command.commands;

import static brainwine.gameserver.command.NotificationType.ALERT;
import static brainwine.gameserver.command.NotificationType.CHAT;
import static brainwine.gameserver.command.NotificationType.SYSTEM;

import brainwine.gameserver.ConsoleThread;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class BroadcastCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        String text = "[Server Notification] " + String.join(" ", args);
        
        // TODO hook the console up to some kind of chat feed?
        if(executor instanceof ConsoleThread) {
            executor.notify(text, CHAT);
        }
        
        for(Player player : GameServer.getInstance().getPlayerManager().getPlayers()) {
            player.notify(text, SYSTEM);
        }
    }
    
    @Override
    public String getName() {
        return "broadcast";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "bc" };
    }
    
    @Override
    public String getDescription() {
        return "Broadcasts a message to all online players.";
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
