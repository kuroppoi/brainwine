package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class HealthMessage extends Message {
    
    public float health;
    
    public HealthMessage(float health) {
        this.health = health;
    }
}
