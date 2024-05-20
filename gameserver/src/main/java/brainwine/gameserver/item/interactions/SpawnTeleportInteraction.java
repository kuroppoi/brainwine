package brainwine.gameserver.item.interactions;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Biome;
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
        
        Player player = (Player)entity;
                
        // Find a random suitable zone
        Zone targetZone = GameServer.getInstance().getZoneManager().getRandomZone(z -> z.getBiome() == Biome.PLAIN);
        
        // Notify the player if no zone could be found
        if(targetZone == null) {
            player.notify("Couldn't find a suitable zone to teleport to. Try again later.");
            return;
        }
        
        // Teleport the player to the target zone
        player.changeZone(targetZone);
    }
}
