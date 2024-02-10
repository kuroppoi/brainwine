package brainwine.gameserver.item.interactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for blocks that may be configured through a dialog
 */
@SuppressWarnings("unchecked")
public class DialogInteraction implements ItemInteraction {
    
    private boolean creationOnly;
    
    public DialogInteraction(boolean creationOnly) {
        this.creationOnly = creationOnly;
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if the required data isn't present
        if(data == null || !(config instanceof Map)) {
            return;
        }
        
        // Do nothing if this block has already been configured and cannot be re-configured with this interaction
        if(creationOnly && metaBlock != null && metaBlock.getBooleanProperty("cd")) {
            return;
        }
        
        Map<String, Object> configMap = (Map<String, Object>)config;
        String target = MapHelper.getString(configMap, "target", "none");
        
        // Do nothing for now if the target isn't the block's metadata
        if(!target.equals("meta")) {
            player.notify("Sorry, this action isn't implemented yet.");
            return;
        }
        
        // Update block metadata
        Map<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> sections = MapHelper.getList(configMap, "sections");
        
        if(metaBlock != null) {
            metadata.putAll(metaBlock.getMetadata());
        }
                
        if(sections != null && data.length == sections.size()) {
            for(int i = 0; i < sections.size(); i++) {
                Map<String, Object> section = sections.get(i);
                String key = MapHelper.getString(section, "input.key");
                
                if(key != null) {
                    String text = String.valueOf(data[i]);
                    
                    // Get rid of text if player is currently muted
                    if(player.isMuted() && MapHelper.getBoolean(section, "input.sanitize")) {
                        text = text.replaceAll(".", "*");
                    }
                    
                    metadata.put(key, text);
                } else if(MapHelper.getBoolean(section, "input.mod")) {
                    List<Object> options = MapHelper.getList(section, "input.options");
                    
                    if(options != null) {
                        mod = options.indexOf(data[i]);
                        mod = mod == -1 ? 0 : mod;
                        mod *= MapHelper.getInt(section, "input.mod_multiple", 1);
                        zone.updateBlock(x, y, layer, item, mod, player);
                    }
                }
            }
        }
        
        // Set configured flag
        if(creationOnly) {
            metadata.put("cd", true);
        }
        
        // Update meta block
        zone.setMetaBlock(x, y, item, player, metadata);
    }
}
