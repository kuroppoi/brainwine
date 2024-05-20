package brainwine.gameserver.item.interactions;

import java.util.Collections;
import java.util.List;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for target teleporters
 */
public class TransmitInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Do nothing if the required data isn't present
        if(data != null || metaBlock == null) {
            return;
        }
        
        Player player = (Player)entity;
        List<List<Integer>> positions = MapHelper.getList(metaBlock.getMetadata(), ">", Collections.emptyList());
        
        // Do nothing if there is no linked position
        if(positions.isEmpty()) {
            return;
        }
        
        List<Integer> position = positions.get(0);
        int targetX = position.get(0);
        int targetY = position.get(1);
        
        // Make sure that the target location is in bounds
        if(!zone.areCoordinatesInBounds(targetX, targetY)) {
            return;
        }
        
        // Notify the player if the target beacon is missing
        if(!zone.getBlock(targetX, targetY).getFrontItem().hasUse(ItemUseType.TRANSMITTED)) {
            player.notify("There is no beacon at the target location.");
            return;
        }
        
        player.teleport(targetX, targetY);
    }
}
