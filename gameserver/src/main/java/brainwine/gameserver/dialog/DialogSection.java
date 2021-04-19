package brainwine.gameserver.dialog;

import java.util.HashMap;
import java.util.Map;

public class DialogSection implements DialogComponent {
    
    private final Map<String, Object> config = new HashMap<>();
    
    public void setTitle(String title) {
        config.put("title", title);
    }
    
    public void setText(String text) {
        config.put("text", text);
    }
    
    public void setInput(DialogInput input) {
        config.put("input", input.getClientConfig());
    }
    
    @Override
    public Map<String, Object> getClientConfig() {
        return config;
    }
}
