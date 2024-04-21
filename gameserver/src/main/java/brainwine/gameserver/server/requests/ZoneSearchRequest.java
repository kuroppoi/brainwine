package brainwine.gameserver.server.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.ZoneSearchMessage;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

@RequestInfo(id = 23)
public class ZoneSearchRequest extends PlayerRequest {
    
    public String type;
    
    @Override
    public void process(Player player) {
        List<Zone> result = new ArrayList<>();
        List<Zone> zones = searchZones(GameServer.getInstance().getZoneManager());
        zones.remove(player.getZone());
        Collections.shuffle(zones);
        Set<Integer> indices = new HashSet<>();
        int index = (int)(Math.random() * zones.size());
        int amount = Math.min(9, zones.size());
        
        for(int i = 0; i < amount; i++) {
            while(indices.contains(index)) {
                index = (int)(Math.random() * zones.size());
            }
            
            indices.add(index);
            result.add(zones.get(i));
        }
        
        player.sendDelayedMessage(new ZoneSearchMessage(type, result, 0));
    }
    
    private List<Zone> searchZones(ZoneManager manager) {
        List<Zone> zones = new ArrayList<>();
        
        switch(type) {
        case "Random":
            zones.addAll(manager.searchZones(null, null));
            break;
        case "Plain":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.PLAIN));
            break;
        case "Arctic":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.ARCTIC));
            break;
        case "Hell":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.HELL));
            break;
        case "Desert":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.DESERT));
            break;
        case "Brain":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.BRAIN));
            break;
        case "Deep":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.DEEP));
            break;
        case "Space":
            zones.addAll(manager.searchZones(zone -> zone.getBiome() == Biome.SPACE));
            break;
        case "Unexplored":
            zones.addAll(manager.searchZones(Zone::isUnexplored, (a, b) -> Float.compare(a.getExplorationProgress(), b.getExplorationProgress())));
            break;
        case "Popular":
            zones.addAll(manager.searchZones(Zone::isPopular, (a, b) -> Integer.compare(b.getPlayers().size(), a.getPlayers().size())));
            break;
        default:
            zones.addAll(manager.searchZones(zone -> zone.getName().toLowerCase().contains(type.toLowerCase())));
            break;
        }
        
        return zones;
    }
}
