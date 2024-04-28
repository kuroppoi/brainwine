package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for items that can spawn entities
 */
public class SpawnInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if item can't spawn entities
        if(!item.hasEntitySpawns() || mod != 0) {
            return;
        }
        
        // Try to spawn the entity and update block mod
        if(zone.spawnEntity(item.getEntitySpawns().next(), x, y) != null) {
            zone.updateBlock(x, y, layer, item, 1);
        }
    }
}
