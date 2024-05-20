package brainwine.gameserver.server.messages;

import brainwine.gameserver.player.ChatType;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 13, collection = true)
public class ChatMessage extends Message {
    
    public int entityId;
    public String message;
    public ChatType type;
    
    public ChatMessage(int entityId, String message, ChatType type) {
        this.entityId = entityId;
        this.message = message;
        this.type = type;
    }
}
