package brainwine.gameserver.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MapHelper {
    
    public static void put(Map<?, ?> map, String path, Object value) {
        String[] segments = path.split("\\.");
        Map<Object, Object> current = (Map<Object, Object>)map;
        
        for(int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            
            if(i + 1 == segments.length) {
                current.put(segment, value);
                return;
            }
            
            Object object = current.get(segment);
            
            if(!(object instanceof Map)) {
                current.put(segment, object = new HashMap<>());
            }
            
            current = (Map<Object, Object>)object;
        }
    }
    
    public static <T> T get(Map<?, ?> map, String path, Class<T> type) {
        return get(map, path, type, null);
    }
    
    public static <T> T get(Map<?, ?> map, String path, Class<T> type, T def) {
        String[] segments = path.split("\\.");
        Map<Object, Object> current = (Map<Object, Object>)map;
        
        for(int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            Object object = current.get(segment);
            
            if(object != null) {
                if(i + 1 == segments.length && type.isAssignableFrom(object.getClass())) {
                    return (T)object;
                } else if(object instanceof Map) {
                    current = (Map<Object, Object>)object;
                }
            } else {
                return def;
            }
        }
        
        return def;
    }
    
    public static String getString(Map<?, ?> map, String path) {
        return getString(map, path, null);
    }
    
    public static String getString(Map<?, ?> map, String path, String def) {
        return get(map, path, String.class, def);
    }
    
    public static byte getByte(Map<?, ?> map, String path) {
        return getByte(map, path, (byte)0);
    }
    
    public static byte getByte(Map<?, ?> map, String path, byte def) {
        return get(map, path, Byte.class, def);
    }
    
    public static short getShort(Map<?, ?> map, String path) {
        return getShort(map, path, (short)0);
    }
    
    public static short getShort(Map<?, ?> map, String path, short def) {
        return get(map, path, Short.class, def);
    }
    
    public static int getInt(Map<?, ?> map, String path) {
        return getInt(map, path, 0);
    }
    
    public static int getInt(Map<?, ?> map, String path, int def) {
        return get(map, path, Integer.class, def);
    }
    
    public static float getFloat(Map<?, ?> map, String path) {
        return getFloat(map, path, 0);
    }
    
    public static float getFloat(Map<?, ?> map, String path, float def) {
        return get(map, path, Float.class, def);
    }
    
    public static long getLong(Map<?, ?> map, String path) {
        return getLong(map, path, 0);
    }
    
    public static long getLong(Map<?, ?> map, String path, long def) {
        return get(map, path, Long.class, def);
    }
    
    public static double getDouble(Map<?, ?> map, String path) {
        return getDouble(map, path, 0);
    }
    
    public static double getDouble(Map<?, ?> map, String path, double def) {
        return get(map, path, Double.class, def);
    }
    
    public static <T> List<T> getList(Map<?, ?> map, String path) {
        return getList(map, path, null);
    }
    
    public static <T> List<T> getList(Map<?, ?> map, String path, List<T> def) {
        return get(map, path, List.class, def);
    }
    
    public static <K, V> Map<K, V> getMap(Map<?, ?> map, String path){
        return getMap(map, path, null);
    }
    
    public static <K, V> Map<K, V> getMap(Map<?, ?> map, String path, Map<K, V> def){
        return get(map, path, Map.class, def);
    }
}
