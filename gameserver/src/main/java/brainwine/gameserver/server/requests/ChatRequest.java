package brainwine.gameserver.server.requests;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class ChatRequest extends PlayerRequest {
    
    public String recipientName;
    public String text;
    
    @Override
    public void process(Player player) {
        if(text.startsWith(CommandManager.CUSTOM_COMMAND_PREFIX)) {
            CommandManager.executeCommand(player, text.substring(1));
            return;
        }
        
        player.getZone().chat(player, text);
    }
}
