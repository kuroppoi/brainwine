package brainwine.gameserver.server.commands;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 13)
public class ChatCommand extends PlayerCommand {
    
    public String recipientName;
    public String text;
    
    @Override
    public void process(Player player) {
        // TODO configurable prefix
        if(text.startsWith("!")) {
            CommandManager.executeCommand(player, text.substring(1));
            return;
        }
        
        player.getZone().chat(player, text);
    }
}
