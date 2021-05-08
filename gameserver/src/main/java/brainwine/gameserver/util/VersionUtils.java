package brainwine.gameserver.util;

public class VersionUtils {
    
    public static int compareVersion(String version, String target) {
        if(version.equals(target)) {
            return 0;
        }
        
        String[] versionSegments = version.split("\\.");
        String[] targetSegments = target.split("\\.");
        int length = Math.max(versionSegments.length, targetSegments.length);
        
        for(int i = 0; i < length; i++) {
            int a = i < versionSegments.length ? Integer.parseInt(versionSegments[i]) : 0;
            int b = i < targetSegments.length ? Integer.parseInt(targetSegments[i]) : 0;
            
            if(a > b) {
                return 1;
            } else if(a < b) {
                return -1;
            }
        }
        
        return 0;
    }
    
    public static boolean isGreaterThan(String version, String target) {
        return compareVersion(version, target) > 0;
    }
    
    public static boolean isGreaterOrEqualTo(String version, String target) {
        return compareVersion(version, target) >= 0;
    }
    
    public static boolean isLessThan(String version, String target) {
        return compareVersion(version, target) < 0;
    }
    
    public static boolean isLessOrEqualTo(String version, String target) {
        return compareVersion(version, target) <= 0;
    }
}
