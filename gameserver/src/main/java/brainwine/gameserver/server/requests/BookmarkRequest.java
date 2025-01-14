package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 62)
public class BookmarkRequest extends PlayerRequest {
    
    public String type; // Looks like multiple bookmark types were planned, but only "zone" ended up being added.
    public String id;
    public boolean active;
    
    @Override
    public void process(Player player) {
        // Check type
        if(!type.equals("zone")) {
            return;
        }
        
        // Check if zone exists
        if(GameServer.getInstance().getZoneManager().getZone(id) == null) {
            return;
        }
        
        // Add or remove bookmark
        if(active) {
            if(player.getBookmarkedZoneCount() > Player.BOOKMARKED_ZONE_LIMIT) {
                player.notify("Error: Bookmark limit reached, please delete some bookmarks.");
                return;
            }
            
            player.addZoneBookmark(id);
        } else {
            player.removeZoneBookmark(id);
        }
    }

}
