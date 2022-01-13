package brainwine.gameserver.server.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.zone.Zone;

public class ZoneSearchMessage extends Message {
    
    public String type;
    public int typePosition = 0;
    public int totalTypes = 1;
    public List<List<Object>> zones;
    public int followeesActive;
    
    public ZoneSearchMessage(String type, Collection<Zone> zones, int followeesActive) {
        this.type = type;
        this.zones = new ArrayList<>();
        
        for(Zone zone : zones) {
            List<Object> info = new ArrayList<>();
            info.add(zone.getName()); // Should actually be the document ID, but we change zones based on name.
            info.add(zone.getName());
            info.add(zone.getPlayers().size());
            info.add(0); // followees count
            info.add(Collections.EMPTY_LIST); // followees
            info.add(0); // active duration
            info.add((int)(zone.getExplorationProgress() * 100));
            info.add(zone.getBiome());
            info.add("purified");
            info.add("a"); // accessibility
            info.add(0); // protection level
            info.add(null); // scenario
            this.zones.add(info);
        }
        
        this.followeesActive = followeesActive;
    }
}
