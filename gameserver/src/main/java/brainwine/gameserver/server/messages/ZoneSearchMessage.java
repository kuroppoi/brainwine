package brainwine.gameserver.server.messages;

import java.util.Collection;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.models.ZoneSearchData;

@MessageInfo(id = 23)
public class ZoneSearchMessage extends Message {
    
    public String type;
    public int typePosition = 0;
    public int totalTypes = 1;
    public Collection<ZoneSearchData> zones;
    public int followeesActive;
    
    public ZoneSearchMessage(String type, Collection<ZoneSearchData> zones, int followeesActive) {
        this.type = type;
        this.zones = zones;
        this.followeesActive = followeesActive;
    }
}
