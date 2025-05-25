package brainwine.gameserver.util;

/**
 * Mainly random shit from the Unity client.
 * TODO cleanup
 */
public class MathUtils {
    
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp01(t);
    }
    
    public static int clamp(int value, int min, int max) {
        if(value < min) {
            return min;
        } else if(value > max) {
            return max;
        } else {
            return value;
        }
    }
    
    public static double clamp(double value, double min, double max) {
        if(value < min) {
            return min;
        } else if(value > max) {
            return max;
        } else {
            return value;
        }
    }
    
    public static double clamp01(double value) {
        return clamp(value, 0.0F, 1.0F);
    }
    
    public static double distance(double x, double y, double x2, double y2) {
        return Math.hypot(x - x2, y - y2);
    }
    
    public static boolean inRange(double x, double y, double x2, double y2, double range) {
        return distance(x, y, x2, y2) <= range;
    }
}
