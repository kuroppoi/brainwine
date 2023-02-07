package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.ChatType;
import brainwine.gameserver.entity.player.Player;

public class SayCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = ((Player)executor);
        
        if(player.isMuted()) {
            player.notify("You are currently muted. Your chat message was not sent.", SYSTEM);
            return;
        }
        
        String text = String.join(" ", args);
        player.getZone().sendChatMessage(player, text, ChatType.SPEECH);
    }
    
    @Override
    public String getName() {
        return "say";
    }
    
    @Override
    public String getDescription() {
        return "Shows a speech bubble to nearby players.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/say <message>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
