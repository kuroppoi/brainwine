package brainwine.api.config;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiConfig {
    
    public static final ApiConfig DEFAULT_CONFIG = new ApiConfig("127.0.0.1", 5002, 5001, 5003, SslConfig.DEFAULT_CONFIG, Arrays.asList(NewsEntry.DEFAULT_NEWS));
    private final String gameServerIp;
    private final int gameServerPort;
    private final int gatewayPort;
    private final int portalPort;
    private final SslConfig sslConfig;
    private final List<NewsEntry> news;
    
    @ConstructorProperties({"game_server_ip", "game_server_port", "gateway_port", "portal_port", "ssl", "news"})
    public ApiConfig(String gameServerIp, int gameServerPort, int gatewayPort, int portalPort, SslConfig sslConfig, List<NewsEntry> news) {
        this.gameServerIp = gameServerIp;
        this.gameServerPort = gameServerPort;
        this.gatewayPort = gatewayPort;
        this.portalPort = portalPort;
        this.sslConfig = sslConfig == null ? SslConfig.DEFAULT_CONFIG : sslConfig;
        System.out.println("ssl config: " + sslConfig);
        this.news = news;
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
    
    @JsonGetter("ssl")
    public SslConfig getSslConfig() {
        return sslConfig;
    }
    
    public List<NewsEntry> getNews() {
        return news;
    }
}
