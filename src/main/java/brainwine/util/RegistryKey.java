package brainwine.util;

public class RegistryKey {
    
    private final String location;
    private final String name;
    private final String type;
    private final String value;
    
    public RegistryKey(String location, String name, String type, String value) {
        this.location = location;
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
}
