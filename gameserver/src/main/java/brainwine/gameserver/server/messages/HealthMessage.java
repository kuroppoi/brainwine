package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 18)
public class HealthMessage extends Message {
    
    public float health;
    
    public HealthMessage(float health) {
        this.health = health;
    }
}
