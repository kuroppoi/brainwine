package brainwine.api.handlers;

import static brainwine.api.util.ContextUtils.error;
import static brainwine.api.util.ContextUtils.handleQueryParam;

import java.util.List;

import brainwine.api.DataFetcher;
import brainwine.api.models.ZoneInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class ZoneSearchHandler implements Handler {
    
    public static final int PAGE_SIZE = 6;
    private final DataFetcher dataFetcher;
    
    public ZoneSearchHandler(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }
    
    @Override
    public void handle(Context ctx) throws Exception {
        final List<ZoneInfo> zones = (List<ZoneInfo>)dataFetcher.fetchZoneInfo();
        String apiToken = ctx.queryParam("api_token");
        
        if(apiToken == null || !dataFetcher.verifyApiToken(apiToken)) {
            error(ctx, "A valid api token is required for this request.");
            return;
        }
        
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
        
        handleQueryParam(ctx, "protected", boolean.class, locked -> {
            zones.removeIf(zone -> zone.isLocked() != locked);
        });
        
        handleQueryParam(ctx, "residency", String.class, residency -> {
            zones.clear(); // not supported yet
        });
        
        handleQueryParam(ctx, "account", String.class, account -> {
            zones.clear(); // not supported yet
        });
        
        handleQueryParam(ctx, "sort", String.class, sort -> {
            switch(sort) {
            case "popularity":
                zones.removeIf(zone -> zone.getPlayerCount() == 0);
                zones.sort((a, b) -> Integer.compare(b.getPlayerCount(), a.getPlayerCount()));
                break;
            case "created":
                break;
            }
        });
        
        // Page
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = page * PAGE_SIZE;
        ctx.json(zones.subList(fromIndex < 0 ? 0 : fromIndex > zones.size() ? zones.size() : fromIndex, toIndex > zones.size() ? zones.size() : toIndex));
    }

}
