package brainwine.gameserver.commands;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.player.ChatType;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "think", description = "Shows a thought bubble to nearby players.")
public class ThinkCommand extends Command {

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
        player.getZone().sendChatMessage(player, text, ChatType.THOUGHT);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/think <message>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
