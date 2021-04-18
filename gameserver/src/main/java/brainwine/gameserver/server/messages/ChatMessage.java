package brainwine.gameserver.server.messages;

import brainwine.gameserver.entity.player.ChatType;
import brainwine.gameserver.server.Message;

public class ChatMessage extends Message {
    
    public int entityId;
    public String message;
    public ChatType type;
    
    public ChatMessage(int entityId, String message, ChatType type) {
        this.entityId = entityId;
        this.message = message;
        this.type = type;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
