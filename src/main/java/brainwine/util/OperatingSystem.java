package brainwine.util;

public enum OperatingSystem {
        
    WINDOWS,
    MACOS,
    OTHER;
    
    private static OperatingSystem operatingSystem;
    
    public static boolean isWindows() {
        return getOperatingSystem() == WINDOWS;
    }
    
    public static boolean isMacOS() {
        return getOperatingSystem() == MACOS;
    }
    
    public static OperatingSystem getOperatingSystem() {
        if(operatingSystem != null) {
            return operatingSystem;
        }
        
        String name = System.getProperty("os.name").toLowerCase();
        
        if(name.contains("windows")) {
            operatingSystem = WINDOWS;
        } else if(name.contains("mac os")) {
            operatingSystem = MACOS;
        } else {
            operatingSystem = OTHER;
        }
        
        return operatingSystem;
    }
}
