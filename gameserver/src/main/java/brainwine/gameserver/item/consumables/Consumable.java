package brainwine.gameserver.item.consumables;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;

// TODO properly handle dialog cancellation for v2 clients
public interface Consumable {
    
    public void consume(Item item, Player player, Object details);
}
