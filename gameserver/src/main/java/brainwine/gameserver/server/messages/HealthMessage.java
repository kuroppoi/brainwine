package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 18)
public class HealthMessage extends Message {
    
    public float health;
    
    public HealthMessage(float health) {
        this.health = health;
    }
}
