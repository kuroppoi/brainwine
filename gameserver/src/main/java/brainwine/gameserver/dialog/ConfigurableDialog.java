package brainwine.gameserver.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;

@SuppressWarnings("unchecked")
public abstract class ConfigurableDialog implements DialogComponent {
    
    private final Map<String, Object> config = new HashMap<>();
    
    public abstract void init();
    public abstract void handleResponse(Player player, Object[] input);
    
    protected void addSection(DialogSection section) {
        List<Map<String, Object>> sections = (List<Map<String, Object>>)config.getOrDefault("sections", new ArrayList<>());
        sections.add(section.getClientConfig());
        config.put("sections", sections); 
    }
    
    @Override
    public Map<String, Object> getClientConfig() {
        return config;
    }
}
