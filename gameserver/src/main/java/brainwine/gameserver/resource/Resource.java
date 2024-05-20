package brainwine.gameserver.resource;

import java.net.URL;

public class Resource {
    
    private final String name;
    private final String simpleName;
    private final String parentDirectoryName;
    private final URL url;
    
    public Resource(String name, String simpleName, String parentDirectoryName, URL url) {
        this.name = name;
        this.simpleName = simpleName;
        this.parentDirectoryName = parentDirectoryName;
        this.url = url;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSimpleName() {
        return simpleName;
    }
    
    public String getParentDirectoryName() {
        return parentDirectoryName;
    }
    
    public URL getUrl() {
        return url;
    }
}
