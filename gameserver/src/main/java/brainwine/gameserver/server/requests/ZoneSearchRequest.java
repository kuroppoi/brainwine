package brainwine.gameserver.server.requests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.ZoneSearchMessage;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

/**
 * TODO followees 'n junk
 * also sort instead of just filter
 */
public class ZoneSearchRequest extends PlayerRequest {
    
    public String type;
    
    @Override
    public void process(Player player) {
        List<Zone> result = new ArrayList<>();
        List<Zone> zones = new ArrayList<>();
        ZoneManager zoneManager = GameServer.getInstance().getZoneManager();
        
        if(type.equals("Random")) {
            zones.addAll(zoneManager.getZones());
        } else {
            zones = zoneManager.searchZones(getFilter());
        }
        
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
    
    private Predicate<Zone> getFilter() {
        switch(type) {
        case "Plain":
            return zone -> zone.getBiome() == Biome.PLAIN;
        case "Arctic":
            return zone -> zone.getBiome() == Biome.ARCTIC;
        case "Hell":
            return zone -> zone.getBiome() == Biome.HELL;
        case "Desert":
            return zone -> zone.getBiome() == Biome.DESERT;
        case "Brain":
            return zone -> zone.getBiome() == Biome.BRAIN;
        case "Deep":
            return zone -> zone.getBiome() == Biome.DEEP;
        case "Space":
            return zone -> zone.getBiome() == Biome.SPACE;
        case "Unexplored":
            return zone -> zone.getExplorationProgress() < 0.5;
        case "Popular":
            return zone -> !zone.getPlayers().isEmpty();
        default:
            return zone -> zone.getName().toLowerCase().contains(type.toLowerCase());
        }
    }
}
