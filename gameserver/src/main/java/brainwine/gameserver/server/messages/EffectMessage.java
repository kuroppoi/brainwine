package brainwine.gameserver.server.messages;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 30)
public class EffectMessage extends Message {
    
    public int x;
    public int y;
    public String name;
    public Object data;
    
    public EffectMessage(float x, float y, String name, Object data) {
        this.x = (int)(x * Entity.POSITION_MODIFIER);
        this.y = (int)(y * Entity.POSITION_MODIFIER);
        this.name = name;
        this.data = data;
    }
}
