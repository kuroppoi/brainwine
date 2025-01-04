package brainwine.api;

import java.util.Collection;

import brainwine.api.models.ZoneInfo;

public interface DataFetcher {
    
    public boolean isPlayerNameTaken(String name);
    public String registerPlayer(String name);
    public String login(String name, String password);
    public String fetchPlayerName(String name);
    public String fetchPlayerId(String apiToken);
    public boolean verifyAuthToken(String name, String token);
    public ZoneInfo getZoneInfo(String nameOrId);
    public Collection<ZoneInfo> fetchZoneInfo();
    public Collection<ZoneInfo> fetchRecentZoneInfo(String apiToken);
    public Collection<ZoneInfo> fetchBookmarkedZoneInfo(String apiToken);
}
