package brainwine.gameserver.dialog;

import java.util.HashMap;
import java.util.Map;

public class DialogInput implements DialogComponent {
    
    private final Map<String, Object> config = new HashMap<>();
    
    public DialogInput(String title, String type, String key, int max) {
        config.put("title", title);
        config.put("type", type);
        config.put("key", key);
        config.put("max", max);
    }
    
    public void setPassword(boolean flag) {
        if(!flag) {
            config.remove("password");
            return;
        }
        
        config.put("password", flag);
    }
    
    @Override
    public Map<String, Object> getClientConfig() {
        return config;
    }
}
