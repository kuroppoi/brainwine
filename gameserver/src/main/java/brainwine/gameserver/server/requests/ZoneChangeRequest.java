package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

@RequestInfo(id = 24)
public class ZoneChangeRequest extends PlayerRequest {
    
    public String zoneId;
    
    @Override
    public void process(Player player) {
        ZoneManager manager = GameServer.getInstance().getZoneManager();
        Zone zone = manager.getZone(zoneId);
        
        // Get zone by name if ID search yielded no result
        if(zone == null) {
            zone = manager.getZoneByName(zoneId);
        }
        
        if(zone == null) {
            player.notify("Couldn't locate world.");
            return;
        }
        
        if(zone == player.getZone()) {
            player.notify("You're already in " + zone.getName());
            return;
        }
        
        if(!player.isGodMode() && !zone.canJoin(player)) {
            player.notify("You do not belong to that world.");
            return;
        }
        
        player.changeZone(zone);
    }
}
