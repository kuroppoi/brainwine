package brainwine.gameserver.item.interactions;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for lootable containers
 */
public class ContainerInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if metadata is not present
        if(metaBlock == null) {
            return;
        }
        
        String dungeonId = metaBlock.getStringProperty("@");
        
        // Check if container is protected by a dungeon
        if(item.hasUse(ItemUseType.FIELDABLE) && dungeonId != null && zone.isDungeonIntact(dungeonId)) {
            player.notify("This container is secured by protectors in the area.");
            return;
        }
        
        Player owner = GameServer.getInstance().getPlayerManager().getPlayerById(metaBlock.getOwner());
        String specialItem = metaBlock.getStringProperty("$");
        
        // Award loot
        if(specialItem != null) {
            if(specialItem.equals("?")) {
                Loot loot = GameServer.getInstance().getLootManager().getRandomLoot(player, item.getLootCategories());
                
                if(loot == null) {
                    player.notify("No eligible loot could be found for this container.");
                } else {
                    metaBlock.removeProperty("$");
                    player.awardLoot(loot, item.getLootGraphic());
                    player.getStatistics().trackContainerLooted(item);
                }
            }
        } else {
            player.notify("Sorry, this container can't be looted right now.");
        }
        
        // Update container mod
        if(!metaBlock.hasProperty("$")) {
            zone.updateBlock(x, y, Layer.FRONT, item, 0, owner, metaBlock.getMetadata());
        }
    }
}
