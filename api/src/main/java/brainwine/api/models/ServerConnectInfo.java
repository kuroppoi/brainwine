package brainwine.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerConnectInfo {
    
    private final String server;
    private final String name;
    private final String token;
    
    public ServerConnectInfo(String server, String name, String token) {
        this.server = server;
        this.name = name;
        this.token = token;
    }
    
    public String getServer() {
        return server;
    }
    
    public String getName() {
        return name;
    }
    
    @JsonProperty("auth_token")
    public String getToken() {
        return token;
    }
}
