package brainwine.api;

import static brainwine.api.util.ContextUtils.error;
import static brainwine.api.util.ContextUtils.handleQueryParam;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.type.TypeReference;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.models.ZoneInfo;
import brainwine.api.util.ImageUtils;
import brainwine.shared.JsonHelper;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;

/**
 * aka Zone Searcher
 */
public class PortalService {
    
    private static final int zoneSearchPageSize = 6;
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, BufferedImage> surfaceMapCache = new HashMap<>();
    private final DataFetcher dataFetcher;
    private final Javalin portal;
    private final Set<WsContext> wsConnections = ConcurrentHashMap.newKeySet();
    
    public PortalService(Api api, int port) {
        this.dataFetcher = api.getDataFetcher();
        logger.info(SERVER_MARKER, "Starting PortalService @ port {} ...", port);
        portal = Javalin.create(config -> config.jsonMapper(new JavalinJackson(JsonHelper.MAPPER)))
            .exception(Exception.class, this::handleException)
            .get("/v1/map/{zone}", this::handleMapRequest)
            .get("/v1/worlds", this::handleZoneSearch)
            .ws("/v1/listen", this::handleWsConfig)
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
     * Handler function for map render requests.
     * TODO throttle
     */
    private void handleMapRequest(Context ctx) throws IOException {
        String apiToken = ctx.queryParam("api_token");
        
        if(apiToken == null || !dataFetcher.verifyApiToken(apiToken)) {
            error(ctx, "A valid api token is required for this request.");
            return;
        }
                
        ZoneInfo zone = dataFetcher.getZoneInfo(ctx.pathParam("zone"));
        
        if(zone == null) {
            error(ctx, "Zone not found.");
            return;
        }
        
        // TODO proper cache management
        if(surfaceMapCache.size() > 50) {
            surfaceMapCache.clear();
        }
        
        BufferedImage image = surfaceMapCache.computeIfAbsent(zone.getName(), x -> MapRenderer.drawSurfaceMap(zone));
        String position = ctx.queryParam("pos");
        
        if(position != null) {
            String[] segments = position.split(",", 2);
            
            if(segments.length != 2) {
                error(ctx, "Position must be formatted as x,y");
                return;
            }
            
            try {
                int x = Integer.parseInt(segments[0]);
                int y = Integer.parseInt(segments[1]);
                image = ImageUtils.copyImage(image); // Copying is important: we do not want to draw to cached images!
                MapRenderer.drawCrossMark(zone, x, y, image);
            } catch(NumberFormatException e) {
                error(ctx, "Coordinates must be valid numbers.");
                return;
            }
        }
        
        ctx.contentType(ContentType.IMAGE_PNG);
        ImageIO.write(image, "png", ctx.res.getOutputStream());
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
        
        // TODO filtering is a bit convoluted, see if we can make it more efficient in the future.
        String account = ctx.queryParam("account");
        final List<ZoneInfo> zones = account == null ? (List<ZoneInfo>)dataFetcher.fetchZoneInfo() 
                : account.equals("recent") ? (List<ZoneInfo>)dataFetcher.fetchRecentZoneInfo(apiToken)
                : account.equals("bookmarked") ? (List<ZoneInfo>)dataFetcher.fetchBookmarkedZoneInfo(apiToken) : new ArrayList<>();
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

    private void handleWsConfig(WsConfig config) {
        config.onConnect(wsConnections::add);
        config.onClose(wsConnections::remove);

        config.onMessage(context -> {
            Map<String, Object> message = JsonHelper.MAPPER.readValue(context.message(), new TypeReference<Map<String, Object>>() {});
            Object messageTypeObj = message.get("type");
            if(!(messageTypeObj instanceof String)) {
                wsError(context, "Bad message type.");
            }
            switch((String)messageTypeObj) {
                default:
                    wsError(context, "Unknown message type: " + (String)messageTypeObj);
            }
        });
    }

    private void wsError(WsContext context, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "error");
        map.put("data", message);

        if(context.session.isOpen()) {
            context.send(map);
        }
    }

    public void broadcast(String type, Object data) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("data", data);
        for(WsContext context : wsConnections) {
            if(context.session.isOpen()) {
                context.send(msg);
            }
        }
    }
    
    /**
     * Stops the portal service.
     * @see Javalin#stop()
     */
    public void stop() {
        portal.stop();
    }
}
