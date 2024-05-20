package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for teleporters
 */
public class TeleportInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        Player player = (Player)entity;
        
        // Try to repair teleporter
        if(mod == 0) {
            zone.updateBlock(x, y, layer, item, 1);
            player.getStatistics().trackDiscovery(item);
            player.notify("You repaired a teleporter!", NotificationType.ACCOMPLISHMENT);
            player.notifyPeers(String.format("%s repaired a teleporter.", player.getName()), NotificationType.SYSTEM);
            return;
        }
        
        // Verify data
        if(data == null || data.length != 2 || mod != 1) {
            return;
        }
        
        int targetX = data[0] instanceof Integer ? (int)data[0] : -1;
        int targetY = data[1] instanceof Integer ? (int)data[1] : -1;
        MetaBlock targetMeta = zone.getMetaBlock(targetX, targetY);
        
        // Do nothing if target has no metadata
        if(targetMeta == null) {
            return;
        }
                
        // Teleport the player if the target location is valid
        if((targetMeta.getItem().hasUse(ItemUseType.TELEPORT) && zone.getBlock(targetX, targetY).getFrontMod() == 1) 
                || targetMeta.getItem().hasUse(ItemUseType.ZONE_TELEPORT)) {
            player.teleport(targetX + 1, targetY);
        }
    }
}
