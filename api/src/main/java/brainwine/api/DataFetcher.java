package brainwine.api;

import java.util.Collection;

import brainwine.api.models.ZoneInfo;

public interface DataFetcher {
    
    public boolean isPlayerNameTaken(String name);
    public String registerPlayer(String name);
    public String login(String name, String password);
    public String fetchPlayerName(String name);
    public boolean verifyAuthToken(String name, String token);
    public boolean verifyApiToken(String apiToken);
    public ZoneInfo getZoneInfo(String nameOrId);
    public Collection<ZoneInfo> fetchZoneInfo();
}
