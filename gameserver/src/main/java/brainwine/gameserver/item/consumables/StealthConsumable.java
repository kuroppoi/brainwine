package brainwine.gameserver.item.consumables;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;

/**
 * Consumable handler for stealth cloaks
 */
public class StealthConsumable implements Consumable {

    @Override
    public void consume(Item item, Player player, Object details) {
        player.getInventory().removeItem(item);
        player.setStealth(true);
        float seconds = item.getPower();
        
        // Apply skill power bonus
        if(item.hasPowerBonus()) {
            seconds += player.getTotalSkillLevel(item.getPowerBonus().getFirst()) * item.getPowerBonus().getLast();
        }
        
        // Create timer
        long delay = (long)(seconds * 1000);
        player.addTimer("end stealth", delay, () -> player.setStealth(false));
    }
}
