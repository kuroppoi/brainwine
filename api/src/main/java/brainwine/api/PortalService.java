package brainwine.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.zone.Zone;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * aka Zone Searcher
 */
public class PortalService {
    
    public static final int PAGE_SIZE = 6;
    private static final Logger logger = LogManager.getLogger();
    private final Javalin portal;
    
    public PortalService(int port) {
        logger.info("Starting PortalService @ port {} ...", port);
        portal = Javalin.create().start(port);
        portal.get("/v1/worlds", ctx -> {
            List<Zone> zones = new ArrayList<>();
            zones.addAll(GameServer.getInstance().getZoneManager().getZones());
            List<Map<String, Object>> zoneInfoList = new ArrayList<>();
            int page = 1;
            
            // Filtering
            if(hasQueryParam(ctx, "page")) {
                page = Integer.parseInt(ctx.queryParam("page"));
            }
            
            if(hasQueryParam(ctx, "biome")) {
                String value = ctx.queryParam("biome");
                zones = filterZones(zones, zone -> zone.getBiome().toString().equalsIgnoreCase(value));
            }
            
            if(hasQueryParam(ctx, "name")) {
                String value = ctx.queryParam("name");
                zones = filterZones(zones, zone -> zone.getName().toLowerCase().contains(value.toLowerCase()));
            }
            
            if(hasQueryParam(ctx, "sort")) {
                String value = ctx.queryParam("sort");
                
                switch(value) {
                case "popularity":
                    zones = filterZones(zones, zone -> zone.getPlayers().size() > 0);
                    break;
                default:
                    break;
                }
            }
            
            // Page
            int fromIndex = (page - 1) * PAGE_SIZE;
            int toIndex = page * PAGE_SIZE;
            zones = zones.subList(fromIndex < 0 ? 0 : fromIndex > zones.size() ? zones.size() : fromIndex, toIndex > zones.size() ? zones.size() : toIndex);
            
            // Compile info
            for(Zone zone : zones) {
                zoneInfoList.add(zone.getPortalConfig());
            }
            
            ctx.json(zoneInfoList);
        });
    }
    
    public void stop() {
        portal.stop();
    }
    
    private boolean hasQueryParam(Context ctx, String param) {
        return ctx.queryParam(param) != null;
    }
    
    private List<Zone> filterZones(Collection<Zone> zones, Predicate<? super Zone> predicate){
        return zones.stream().filter(predicate).collect(Collectors.toList());
    }
}
