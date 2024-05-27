package brainwine.gameserver.server.requests;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.server.messages.ZoneSearchMessage;
import brainwine.gameserver.server.models.ZoneSearchData;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 23)
public class ZoneSearchRequest extends PlayerRequest {
    
    public String type;
    
    @Override
    public void process(Player player) {
        Collection<Zone> allZones = GameServer.getInstance().getZoneManager().getZones();
        int searchLimit = 200;
        int displayLimit = 10;
        
        // Find zones matching the filter
        List<Zone> zones = allZones.stream()
                .filter(getFilter(player).and(zone -> zone != player.getZone()))
                .limit(searchLimit)
                .collect(Collectors.toList());
        
        // Get random zones to display
        List<ZoneSearchData> data = zones.stream()
                .skip(ThreadLocalRandom.current().nextInt(Math.max(1, 1 + zones.size() - displayLimit)))
                .limit(displayLimit)
                .sorted(getComparator())
                .map(zone -> new ZoneSearchData(zone, player))
                .collect(Collectors.toList());
        
        // Send data
        player.sendDelayedMessage(new ZoneSearchMessage(type, data, 0));
    }
    
    private Predicate<Zone> getFilter(Player player) {
        switch(type) {
            case "Random":
                return zone -> zone.isPublic();
            case "Popular":
                return zone -> zone.isPublic() && zone.isPopular();
            case "Unexplored":
                return zone -> zone.isPublic() && !zone.isProtected() && zone.isUnexplored();
            case "Owned":
                return zone -> zone.isOwner(player);
            case "Member":
                return zone -> zone.isMember(player);
            case "Plain":
            case "Hell":
            case "Arctic":
            case "Desert":
            case "Brain":
            case "Deep":
            case "Space":
                return zone -> zone.isPublic() && !zone.isProtected() && zone.getBiome() == Biome.valueOf(type.toUpperCase());
            default:
                return zone -> zone.isPublic() && zone.getName().toLowerCase().contains(type.toLowerCase());
        }
    }
    
    private Comparator<Zone> getComparator() {
        switch(type) {
        case "Popular":
            return (a, b) -> Integer.compare(b.getPlayerCount(), a.getPlayerCount()); // Most players first
        case "Unexplored":
            return (a, b) -> Integer.compare(a.getChunksExploredCount(), b.getChunksExploredCount()); // Least explored first
        default:
            return (a, b) -> 0;
        }
    }
}
