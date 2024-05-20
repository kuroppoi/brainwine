package brainwine.gameserver.item.consumables;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;

public interface Consumable {
    
    public void consume(Item item, Player player, Object details);
}
