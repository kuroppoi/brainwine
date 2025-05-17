package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Inventory;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

import java.util.HashMap;
import java.util.Map;

public class BatteryInteraction implements ItemInteraction {
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        if(!entity.isPlayer()) return;
        Player player = (Player)entity;

        Inventory inventory = player.getInventory();
        Item battery = ItemRegistry.getItem("accessories/battery");

        // Check if player has the required items
        if(!inventory.hasItem(battery)) {
            player.notify("You don't have any batteries.");
            return;
        }

        Player owner = metaBlock != null ? metaBlock.getOwner() : null;
        Map<String, Object> metaData = metaBlock != null ? new HashMap<>(metaBlock.getMetadata()) : new HashMap<>();

        long addition = (long)(1000L * battery.getPower());
        long currentTime = System.currentTimeMillis();
        long currentFinalTime = MapHelper.getLong(metaData, "f", 0L);

        // Check if not enough time has elapsed since last installation
        if(addition - (currentFinalTime - currentTime) < Math.min(0.5 * addition, 600_000)) {
            player.notify("Wait a bit more before installing another battery.");
            return;
        }

        inventory.removeItem(battery, true);
        metaData.put("f", System.currentTimeMillis() + addition);
        zone.updateBlock(x, y, layer, item, 1, owner);
        zone.setMetaBlock(x, y, item, owner, metaData);
        zone.spawnEffect(x + 2.0F, y, "area steam", 10);
    }
}
