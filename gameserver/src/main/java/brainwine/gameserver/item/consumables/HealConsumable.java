package brainwine.gameserver.item.consumables;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;

/**
 * Consumable handler for healing items
 */
public class HealConsumable implements Consumable {

    @Override
    public void consume(Item item, Player player, Object details) {
        player.heal(item.getPower());
        player.getInventory().removeItem(item);
    }
}
