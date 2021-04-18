package brainwine.gameserver.server.messages;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.server.Message;

public class EffectMessage extends Message {
    
    public int x;
    public int y;
    public String name;
    public int count;
    
    public EffectMessage(float x, float y, String name, int count) {
        this.x = (int)(x * Entity.POSITION_MODIFIER);
        this.y = (int)(y * Entity.POSITION_MODIFIER);
        this.name = name;
        this.count = count;
    }
}
