package brainwine.gameserver.item.consumables;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;

public interface Consumable {
	
	public void consume(Item item, Player player);
}
