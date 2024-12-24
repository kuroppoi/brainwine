package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.models.PlayerStat;

@MessageInfo(id = 44, collection = true)
public class StatMessage extends Message {
    
    public PlayerStat stat;
    public Object value;
    
    public StatMessage(PlayerStat stat, Object value) {
        this.stat = stat;
        this.value = value;
    }
}
