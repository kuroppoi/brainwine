package brainwine.gameserver.item.consumables;

import java.util.List;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.zone.MetaBlock;

/**
 * Consumable handler for portable teleporters.
 * 
 * These do not seem to function correctly on v3 clients.
 */
public class TeleportConsumable implements Consumable {
    
    @Override
    public void consume(Item item, Player player, Object details) {
        // Verify details
        if(details == null || !(details instanceof List)) {
            fail(item, player);
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> coordinates = (List<Object>)details;
        
        // Verify coordinates
        if(coordinates.size() != 2 || !(coordinates.get(0) instanceof Integer) || !(coordinates.get(1) instanceof Integer)) {
            fail(item, player);
            return;
        }
        
        int x = (int)coordinates.get(0);
        int y = (int)coordinates.get(1);
        MetaBlock block = player.getZone().getMetaBlock(x, y);
        
        // Check if there is a teleporter at the target location
        if(block == null || !block.getItem().hasUse(ItemUseType.TELEPORT, ItemUseType.ZONE_TELEPORT)) {
            fail(item, player);
            return;
        }
        
        player.getInventory().removeItem(item);
        player.teleport(x, y);
    }
    
    private void fail(Item item, Player player) {
        player.notify("Oops! There was a problem teleporting you to your target destination.");
        player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
