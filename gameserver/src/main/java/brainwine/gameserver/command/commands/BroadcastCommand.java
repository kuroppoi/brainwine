package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class BroadcastCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.sendMessage(String.format("Usage: %s", getUsage()));
            return;
        }
        
        String text = "[Server Notification] " + String.join(" ", args);
        
        // TODO hook the console up to some kind of chat feed?
        if(executor instanceof GameServer) {
            executor.sendMessage(text);
        }
        
        for(Player player : GameServer.getInstance().getPlayerManager().getPlayers()) {
            player.notify(text, 11);
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
        return "Broadcasts a message to the entire server.";
    }
    
    @Override
    public String getUsage() {
        return "/broadcast <message>";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
