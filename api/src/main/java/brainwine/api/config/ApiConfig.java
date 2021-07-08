package brainwine.api.config;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiConfig {
    
    public static final ApiConfig DEFAULT_CONFIG = new ApiConfig("127.0.0.1", 5002, 5001, 5003, Arrays.asList(NewsEntry.DEFAULT_NEWS));
    private final String gameServerIp;
    private final int gameServerPort;
    private final int gatewayPort;
    private final int portalPort;
    private final List<NewsEntry> news;
    
    @ConstructorProperties({"game_server_ip", "game_server_port", "gateway_port", "portal_port", "news"})
    public ApiConfig(String gameServerIp, int gameServerPort, int gatewayPort, int portalPort, List<NewsEntry> news) {
        this.gameServerIp = gameServerIp;
        this.gameServerPort = gameServerPort;
        this.gatewayPort = gatewayPort;
        this.portalPort = portalPort;
        this.news = news;
        Collections.reverse(this.news);
    }
    
    public String getGameServerIp() {
        return gameServerIp;
    }
    
    public int getGameServerPort() {
        return gameServerPort;
    }
    
    public int getGatewayPort() {
        return gatewayPort;
    }
    
    public int getPortalPort() {
        return portalPort;
    }
    
    public List<NewsEntry> getNews() {
        return news;
    }
}
