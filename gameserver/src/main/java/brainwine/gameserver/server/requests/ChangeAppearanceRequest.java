package brainwine.gameserver.server.requests;

import java.util.Map;
import java.util.Map.Entry;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.player.Appearance;
import brainwine.gameserver.player.AppearanceSlot;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.util.MapHelper;

@RequestInfo(id = 22)
public class ChangeAppearanceRequest extends PlayerRequest {
    
    public Map<String, Object> appearance;
    
    @Override
    public void process(Player player) {
        // Handle special cases
        if(appearance.containsKey("meta")) {
            String meta = MapHelper.getString(appearance, "meta", "");

            if(meta.equals("randomize")) {
                player.randomizeAppearance();
            } else {
                player.showDialog(DialogHelper.getWardrobeDialog(meta));
            }
            
            return;
        }
        
        // Validate appearance data
        for(Entry<String, Object> entry : appearance.entrySet()) {
            AppearanceSlot slot = AppearanceSlot.fromId(entry.getKey());
            Object value = entry.getValue();
            
            // Fail if slot is not valid
            if(slot == null || !slot.isChangeable()) {
                fail(player);
                return;
            }
            
            // Handle color data
            if(slot.isColor()) {
                // Fail if color value is not a string
                if(!(value instanceof String)) {
                    fail(player);
                    return;
                }
                
                // Fail if player doesn't own color
                if(!Appearance.getAvailableColors(slot, player).contains((String)value)) {
                    fail(player);
                    return;
                }
                
                continue;
            }
            
            // Fail if item value is not an integer (item code)
            if(!(value instanceof Integer)) {
                fail(player);
                return;
            }
            
            Item item = ItemRegistry.getItem((int)value);
            
            // Do nothing if item isn't valid clothing or player doesn't own it
            if(!item.isClothing() || !slot.getCategory().equals(item.getCategory()) || (!item.isBase() && !player.getInventory().hasItem(item))) {
                fail(player);
                return;
            }
        }
        
        // Update player appearance
        player.updateAppearance(appearance);
    }
    
    private void fail(Player player) {
        player.sendMessage(new EntityChangeMessage(player.getId(), player.getAppearance()));
    }
}
