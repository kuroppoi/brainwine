package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.messages.EventMessage;

public class RickrollCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(player == null) {
            executor.notify("This player does not exist.", ALERT);
            return;
        } else if(!player.isOnline()) {
            executor.notify("This player is offline.", ALERT);
            return;
        } else if(!player.isV3()) {
            executor.notify("Cannot open URLs on iOS clients.", ALERT);
            return;
        }
        
        player.sendMessage(new EventMessage("openUrl", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        executor.notify(String.format("Successfully rickrolled %s!", player.getName()), ALERT);
    }

    @Override
    public String getName() {
        return "rickroll";
    }
    
    @Override
    public String getDescription() {
        return "Makes a player hate you forever.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/rickroll <player>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
