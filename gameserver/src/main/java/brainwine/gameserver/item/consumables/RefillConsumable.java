package brainwine.gameserver.item.consumables;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;

/**
 * Consumable handler for steam canisters
 */
public class RefillConsumable implements Consumable {

    @Override
    public void consume(Item item, Player player, Object details) {
        // All we do is remove the item because steam functionality is pretty much entirely client-side
        player.getInventory().removeItem(item);
    }
}
