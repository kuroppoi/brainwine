package brainwine.gameserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Configuration extends HashMap<String, Object> {
    
    /**
     * 
     */
    private static final long serialVersionUID = -860160965655378037L;
    
    public Configuration() {
        super();
    }
    
    public Configuration(Map<String, Object> config) {
        super(config);
    }
    
    public void putObject(String path, Object value) {
        String[] segments = path.split("\\.");
        Map<String, Object> current = this;
        
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
            
            current = (Map<String, Object>)object;
        }
    }
    
    public <T> T getObject(String path, Class<T> type) {
        return getObject(path, type, null);
    }
    
    public <T> T getObject(String path, Class<T> type, T def) {
        String[] segments = path.split("\\.");
        Map<String, Object> current = this;
        
        for(int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            Object object = current.get(segment);
            
            if(object != null) {
                if(i + 1 == segments.length && type.isAssignableFrom(object.getClass())) {
                    return (T)object;
                } else if(object instanceof Map) {
                    current = (Map<String, Object>)object;
                }
            } else {
                return def;
            }
        }
        
        return def;
    }
    
    public String getString(String path, String def) {
        return getObject(path, String.class, def);
    }
    
    public String getString(String path) {
        return getString(path, null);
    }
    
    public int getInt(String path, int def) {
        return getObject(path, Integer.class, def);
    }
    
    public int getInt(String path) {
        return getInt(path, 0);
    }
    
    public List<Object> getList(String path, List<Object> def){
        return getObject(path, List.class, def);
    }
    
    public List<Object> getList(String path) {
        return getList(path, null);
    }
    
    public Map<String, Object> getMap(String path, Map<String, Object> def) {
        return getObject(path, Map.class, def);
    }
    
    public Map<String, Object> getMap(String path) {
        return getMap(path, null);
    }
}
