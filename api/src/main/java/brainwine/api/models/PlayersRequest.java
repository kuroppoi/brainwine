package brainwine.api.models;

import java.beans.ConstructorProperties;

public class PlayersRequest {
    
    private final String platform;
    private final String version;
    private final boolean unity;
    private final String name;
    
    @ConstructorProperties({"platform", "version", "unity", "name", "premium"})
    public PlayersRequest(String platform, String version, boolean unity, String name, boolean premium) {
        this.platform = platform;
        this.version = version;
        this.unity = unity;
        this.name = name;
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
}
