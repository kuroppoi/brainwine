package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public interface ItemInteraction {
    
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data);
}
