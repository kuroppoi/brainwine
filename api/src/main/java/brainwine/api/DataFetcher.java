package brainwine.api;

import java.util.Collection;

import brainwine.api.models.ZoneInfo;

public interface DataFetcher {
    
    public boolean isPlayerNameTaken(String name);
    public String registerPlayer(String name);
    public String login(String name, String password);
    public boolean verifyAuthToken(String name, String token);
    public boolean verifyApiToken(String apiToken);
    public Collection<ZoneInfo> fetchZoneInfo();
}
