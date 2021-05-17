package brainwine.gameserver.server.requests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.entity.player.ClothingSlot;
import brainwine.gameserver.entity.player.ColorSlot;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.msgpack.models.AppearanceData;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.DialogMessage;
import brainwine.gameserver.util.MapHelper;

/**
 * TODO we should actually check if the sent value is even compatible with the slot.
 * We wouldn't want to allow players to equip pants for hats!
 */
public class ChangeAppearanceRequest extends PlayerRequest {
    
    public AppearanceData data;
    
    @Override
    public void process(Player player) {
        if(data.containsKey("meta")) {
            String meta = "" + data.get("meta");

            if(meta.equals("randomize")) {
                player.alert("Sorry, you can't randomize your appearance yet.");
            } else {
                Map<String, Object> panel = MapHelper.getMap(GameConfiguration.getBaseConfig(), String.format("wardrobe_panel.dialogs.%s", meta));
                
                if(panel != null) {
                    Map<String, Object> dialog = new HashMap<>();
                    Map<String, Object> section = new HashMap<>();
                    section.put("input", panel);
                    dialog.put("sections", Arrays.asList(section));
                    dialog.put("alignment", "left");
                    dialog.put("target", "appearance");
                    
                    // No input, just send the message directly.
                    player.sendMessage(new DialogMessage(-1, dialog));
                }
            }
            
            return;
        }
        
        for(Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if(value instanceof Integer) {
                ClothingSlot slot = ClothingSlot.fromId(key);
                
                if(slot == null) {
                    continue;
                }
                
                Item item = ItemRegistry.getItem((int)value);
                
                if(!player.hasClothing(item)) {
                    player.alert("Sorry, but you do not own this.");
                    return;
                }
                
                player.setClothing(slot, item);
            } else if(value instanceof String) {
                // TODO check if player owns color
                ColorSlot slot = ColorSlot.fromId(key);
                String color = (String)value;
                
                if(slot == null) {
                    continue;
                }
                
                player.setColor(slot, color);
            }
        }
    }
}
