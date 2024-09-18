package brainwine.api;

import static brainwine.api.util.ContextUtils.error;
import static brainwine.api.util.ContextUtils.handleQueryParam;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.models.ZoneInfo;
import brainwine.shared.JsonHelper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;

/**
 * aka Zone Searcher
 */
public class PortalService {
    
    private static final int zoneSearchPageSize = 6;
    private static final Logger logger = LogManager.getLogger();
    private final DataFetcher dataFetcher;
    private final Javalin portal;
    
    public PortalService(Api api, int port) {
        this.dataFetcher = api.getDataFetcher();
        logger.info(SERVER_MARKER, "Starting PortalService @ port {} ...", port);
        portal = Javalin.create(config -> config.jsonMapper(new JavalinJackson(JsonHelper.MAPPER)))
            .exception(Exception.class, this::handleException)
            .get("/v1/worlds", this::handleZoneSearch)
            .start(port);
    }
    
    /**
     * Exception handler function.
     */
    private void handleException(Exception exception, Context ctx) {
        logger.error(SERVER_MARKER, "Exception caught", exception);
        error(ctx, "%s", exception);
    }
    
    /**
     * Handler function for zone search requests.
     * TODO could use some work.
     */
    private void handleZoneSearch(Context ctx) {
        String apiToken = ctx.queryParam("api_token");
        
        if(apiToken == null || !dataFetcher.verifyApiToken(apiToken)) {
            error(ctx, "A valid api token is required for this request.");
            return;
        }
        
        final List<ZoneInfo> zones = (List<ZoneInfo>)dataFetcher.fetchZoneInfo(); // TODO this will probably be slow if there is a large number of zones
        zones.removeIf(zone -> zone.isPrivate() && !apiToken.equals(zone.getOwner()) && !zone.getMembers().contains(apiToken));
        
        handleQueryParam(ctx, "name", String.class, name -> {
            zones.removeIf(zone -> !zone.getName().toLowerCase().contains(name.toLowerCase()));
        });
        
        handleQueryParam(ctx, "activity", String.class, activity -> {
            zones.removeIf(zone -> zone.getActivity() == null || !zone.getActivity().equalsIgnoreCase(activity));
        });
        
        handleQueryParam(ctx, "biome", String.class, biome -> {
            zones.removeIf(zone -> !zone.getBiome().equalsIgnoreCase(biome));
        });
        
        handleQueryParam(ctx, "pvp", boolean.class, pvp -> {
            zones.removeIf(zone -> zone.isPvp() != pvp);
        });
        
        handleQueryParam(ctx, "protected", boolean.class, value -> {
            zones.removeIf(zone -> zone.isProtected() != value);
        });
        
        handleQueryParam(ctx, "residency", String.class, residency -> {
            switch(residency) {
            case "owned":
                zones.removeIf(zone -> !apiToken.equals(zone.getOwner()));
                break;
            case "member":
                zones.removeIf(zone -> !zone.getMembers().contains(apiToken));
                break;
            default:
                zones.clear();
                break;
            }
        });
        
        handleQueryParam(ctx, "account", String.class, account -> {
            zones.clear(); // not supported yet
        });
        
        handleQueryParam(ctx, "sort", String.class, sort -> {
            switch(sort) {
            case "popularity": // Sort by most players first
                //zones.removeIf(zone -> zone.getPlayerCount() == 0);
                zones.sort((a, b) -> Integer.compare(b.getPlayerCount(), a.getPlayerCount()));
                break;
            case "created": // Sort by newest first
                zones.sort((a, b) -> b.getCreationDate().compareTo(a.getCreationDate()));
                break;
            }
        });
        
        // Page
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int fromIndex = (page - 1) * zoneSearchPageSize;
        int toIndex = page * zoneSearchPageSize;
        ctx.json(zones.subList(fromIndex < 0 ? 0 : fromIndex > zones.size() ? zones.size() : fromIndex, toIndex > zones.size() ? zones.size() : toIndex));
    }
    
    /**
     * Stops the portal service.
     * @see Javalin#stop()
     */
    public void stop() {
        portal.stop();
    }
}
