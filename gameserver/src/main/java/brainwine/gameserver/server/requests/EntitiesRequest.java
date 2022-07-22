package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.EntityStatusMessage;

@RequestInfo(id = 51)
public class EntitiesRequest extends PlayerRequest {
    
    public int[] entityIds;
    
    public void process(Player player) {
        int count = Math.min(entityIds.length, 10);
        
        for(int i = 0; i < count; i++) {
            Entity entity = player.getZone().getEntity(entityIds[i]);
            
            if(entity != null && player.isTrackingEntity(entity)) {
                player.sendMessage(new EntityStatusMessage(entity, EntityStatus.ENTERING));
            }
        }
    }
}
