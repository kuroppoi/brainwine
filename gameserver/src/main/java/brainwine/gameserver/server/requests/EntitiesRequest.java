package brainwine.gameserver.server.requests;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.server.models.EntityStatusData;

@RequestInfo(id = 51)
public class EntitiesRequest extends PlayerRequest {
    
    public int[] entityIds;
    
    public void process(Player player) {
        int count = Math.min(entityIds.length, 10);
        
        if(count > 0) {
            List<EntityStatusData> statuses = new ArrayList<>();
            
            for(int i = 0; i < count; i++) {
                Entity entity = player.getZone().getEntity(entityIds[i]);
                
                if(entity != null && player.isTrackingEntity(entity)) {
                    statuses.add(EntityStatusData.entering(entity));
                }
            }
            
            if(!statuses.isEmpty()) {
                player.sendMessage(new EntityStatusMessage(statuses));
            }
        }
    }
}
