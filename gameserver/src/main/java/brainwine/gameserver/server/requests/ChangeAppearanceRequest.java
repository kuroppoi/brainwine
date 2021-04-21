package brainwine.gameserver.server.requests;

import java.util.Map.Entry;

import brainwine.gameserver.entity.player.ClothingSlot;
import brainwine.gameserver.entity.player.ColorSlot;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.msgpack.models.AppearanceData;
import brainwine.gameserver.server.PlayerRequest;

/**
 * TODO we should actually check if the sent value is even compatible with the slot.
 * We wouldn't want to allow players to equip pants for hats!
 */
public class ChangeAppearanceRequest extends PlayerRequest {
    
    public AppearanceData data;
    
    @Override
    public void process(Player player) {
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
