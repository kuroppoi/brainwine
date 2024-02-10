package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for blocks that can change between two states
 */
public class ChangeInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        zone.updateBlock(x, y, layer, item, mod == 0 ? 1 : 0, player);
    }
}
