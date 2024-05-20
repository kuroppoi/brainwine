package brainwine.gameserver.server.requests;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 13)
public class ChatRequest extends PlayerRequest {
    
    @OptionalField
    public String recipientName;
    public String text;
    
    @Override
    public void process(Player player) {
        if(text.startsWith(CommandManager.CUSTOM_COMMAND_PREFIX)) {
            CommandManager.executeCommand(player, text.substring(1));
            return;
        }
        
        if(player.isMuted()) {
            player.notify("You are currently muted. Your chat message was not sent.", NotificationType.SYSTEM);
            return;
        }
        
        player.getZone().sendChatMessage(player, text);
    }
}
