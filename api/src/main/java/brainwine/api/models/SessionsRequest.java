package brainwine.api.models;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionsRequest {
    
    private final String platform;
    private final String version;
    private final boolean unity;
    private final String name;
    private final String password;
    private final String token;
    
    @ConstructorProperties({"platform", "version", "unity", "name", "password", "token"})
    public SessionsRequest(String platform, String version, boolean unity, String name, String password, String token) {
        this.platform = platform;
        this.version = version;
        this.unity = unity;
        this.name = name;
        this.password = password;
        this.token = token;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public String getVersion() {
        return version;
    }
    
    public boolean isUnity() {
        return unity;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getToken() {
        return token;
    }
}
