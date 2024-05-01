package brainwine.gameserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.shared.JsonHelper;

@SuppressWarnings("unchecked")
public class MapHelper {
    
    private static final Logger logger = LogManager.getLogger();
    
    public static <K, V> Map<K, V> copy(Map<K, V> map) {
        try {
            return JsonHelper.readValue(map, new TypeReference<Map<K, V>>(){});
        } catch (JsonProcessingException e) {
            logger.error("Map copy failed", e);
        }
        
        return new HashMap<>();
    }
    
    public static <K, V> Map<K, V> map(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    
    public static <K, V> Map<K, V> map(Class<K> keyType, Class<V> valueType, Object... keysAndValues) {
        Map<K, V> map = new HashMap<>();
        
        for(int i = 0; i < keysAndValues.length / 2; i++) {
            Object key = keysAndValues[i * 2];
            Object value = keysAndValues[i * 2 + 1];
            
            if(keyType.isAssignableFrom(key.getClass()) && valueType.isAssignableFrom(value.getClass())) {
                map.put((K)key, (V)value);
            }
        }
        
        return map;
    }
    
    public static void put(Map<?, ?> map, String path, Object value) {
        if(path == null) {
            return;
        }
        
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
        if(path == null) {
            return def;
        }
        
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
        return ((Number)get(map, path, Number.class, def)).byteValue();
    }
    
    public static boolean getBoolean(Map<?, ?> map, String path) {
        return getBoolean(map, path, false);
    }
    
    public static boolean getBoolean(Map<?, ?> map, String path, boolean def) {
        return get(map, path, Boolean.class, def);
    }
    
    public static short getShort(Map<?, ?> map, String path) {
        return getShort(map, path, (short)0);
    }
    
    public static short getShort(Map<?, ?> map, String path, short def) {
        return ((Number)get(map, path, Number.class, def)).shortValue();
    }
    
    public static int getInt(Map<?, ?> map, String path) {
        return getInt(map, path, 0);
    }
    
    public static int getInt(Map<?, ?> map, String path, int def) {
        return ((Number)get(map, path, Number.class, def)).intValue();
    }
    
    public static float getFloat(Map<?, ?> map, String path) {
        return getFloat(map, path, 0);
    }
    
    public static float getFloat(Map<?, ?> map, String path, float def) {
        return ((Number)get(map, path, Number.class, def)).floatValue();
    }
    
    public static long getLong(Map<?, ?> map, String path) {
        return getLong(map, path, 0);
    }
    
    public static long getLong(Map<?, ?> map, String path, long def) {
        return ((Number)get(map, path, Number.class, def)).longValue();
    }
    
    public static double getDouble(Map<?, ?> map, String path) {
        return getDouble(map, path, 0);
    }
    
    public static double getDouble(Map<?, ?> map, String path, double def) {
        return ((Number)get(map, path, Number.class, def)).doubleValue();
    }
    
    public static void appendList(Map<?, ?> map, String path, Object object) {
        List<Object> list = getList(map, path, new ArrayList<>());
        list.add(object);
        put(map, path, list);
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
