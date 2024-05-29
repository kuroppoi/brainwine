package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for that one white teleporter in the tutorial world
 */
public class SpawnTeleportInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Send the player to a suitable beginner zone
        Player player = (Player)entity;
        player.changeZone(null);
    }
}
