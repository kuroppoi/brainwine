package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for items that can spawn entities
 */
public class SpawnInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if item can't spawn entities
        if(!item.hasEntitySpawns() || mod != 0) {
            return;
        }
        
        EntityConfig entityConfig = EntityRegistry.getEntityConfig(item.getEntitySpawns().next());
        
        // Do nothing if type is invalid
        if(entityConfig == null) {
            return;
        }
        
        // Spawn the entity
        Npc entity = new Npc(zone, entityConfig);
        zone.spawnEntity(entity, x, y);
        
        // Update block mod
        zone.updateBlock(x, y, layer, item, 1);
    }
}
