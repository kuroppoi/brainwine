package brainwine.gameserver.server.requests;

import java.util.UUID;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 12)
public class BlockPlaceRequest extends PlayerRequest {

    public int x;
    public int y;
    public Layer layer;
    public Item item;
    public int mod;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        if(player.isDead()) {
            return;
        }
        
        if(!player.isChunkActive(x, y)) {
            player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        if(!player.isGodMode() && !MathUtils.inRange(x, y, player.getX(), player.getY(), player.getPlacementRange())) {
            fail(player, "This block is too far away.");
            return;
        }
        
        if(!player.isGodMode() && !player.getInventory().hasItem(item)) {
            fail(player, "You do not have enough of this item.");
            return;
        }
        
        if(!player.isGodMode() && !item.isPlacable()) {
            fail(player, "This item cannot be placed.");
            return;
        }
        
        if(!player.isGodMode() && item.getLayer() != layer) {
            fail(player, "This item cannot be placed here.");
            return;
        }
        
        if(!player.isGodMode() && !item.canPlaceInField() && zone.isBlockProtected(x, y, player)) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(!player.isGodMode() && zone.isBlockOccupied(x, y, layer)) {
            fail(player, "This block is occupied.");
            return;
        }
        
        if(!player.isGodMode() && item.requiresPlacingSkill()) {
            Pair<Skill, Integer> placingSkill = item.getPlacingSkill();
            
            if(player.getTotalSkillLevel(placingSkill.getFirst()) < placingSkill.getLast()) {
                fail(player, "You are not skilled enough to place this block.");
                return;
            }
        }
        
        if(!player.isGodMode() && item.isDish() && zone.willDishOverlap(x, y, item.getField(), player)) {
            fail(player, "Dish will overlap another protector.");
            return;
        }
        
        if(layer == Layer.LIQUID) {
            mod = 5;
        } else if(item.getMod() == ModType.ROTATION && !item.isMirrorable()) {
            // Automatically orient rotatable blocks based on adjacent block
            if(zone.isChunkLoaded(x, y + 1) && zone.getBlock(x, y + 1).getFrontItem().isWhole()) {
                mod = 0;
            } else if(zone.isChunkLoaded(x, y - 1) && zone.getBlock(x, y - 1).getFrontItem().isWhole()) {
                mod = 2;
            } else if(zone.isChunkLoaded(x - 1, y) && zone.getBlock(x - 1, y).getFrontItem().isWhole()) {
                mod = 1;
            } else if(zone.isChunkLoaded(x + 1, y) && zone.getBlock(x + 1, y).getFrontItem().isWhole()) {
                mod = 3;
            }
        }
        
        zone.updateBlock(x, y, layer, item, mod, player);
        player.getInventory().removeItem(item);
        player.getStatistics().trackItemPlaced();
        player.trackPlacement(x, y, item);
        
        // Create block timer if applicable
        if(item.hasTimer()) {
            createBlockTimer(zone, player);
        }
        
        // Process custom place if applicable
        if(item.hasCustomPlace()) {
            processCustomPlace(zone, player);
        }
    }
    
    private void createBlockTimer(Zone zone, Player player) {
        String type = item.getTimerType();
        int value = item.getTimerValue();
        Runnable task = null;
        
        // TODO implement more block timers
        switch(type) {
        case "bomb":
            task = () -> zone.explode(x, y, value, player, true, value, DamageType.FIRE, value >= 6 ? "bomb-large" : "bomb");
            break;
        case "bomb-fire":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.FIRE, "bomb-fire");
            break;
        case "bomb-electric":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.ENERGY, "bomb-electric");
            break;
        case "bomb-frost":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.COLD, "bomb-frost");
            break;
        case "bomb-dig":
            task = () -> {
                zone.explode(x, y, value, player, "bomb-fire");
                int distance = value * 10;
                
                // Dig until we reach the maximum distance or hit a solid block
                for(int i = 1; i <= distance; i++) {
                    if(!zone.digBlock(x, y + i)) {
                        break;
                    }
                }
            };
            break;
        }
        
        if(task != null) {
            zone.addBlockTimer(x, y, item.getTimerDelay() * 1000, task);
        }
    }
    
    private void processCustomPlace(Zone zone, Player player) {        
        switch(item.getId()) {
            case "building/plug":
                // See if we can plug a maw or pipe
                Item baseItem = zone.getBlock(x, y).getBaseItem();
                String plugged = baseItem.hasId("base/maw") ? "base/maw-plugged"
                        : baseItem.hasId("base/pipe") ? "base/pipe-plugged" : null;
                
                if(plugged != null) {
                    zone.updateBlock(x, y, Layer.FRONT, 0); // Remove the plug front block
                    zone.updateBlock(x, y, Layer.BASE, plugged);
                    player.getStatistics().trackMawPlugged();
                }
                
                break;
            case "containers/chest-plenty":
            case "containers/sack-plenty":
                // Create additional metadata for chests o' plenty
                MetaBlock metaBlock = zone.getMetaBlock(x, y);
                
                if(metaBlock != null) {
                    metaBlock.setProperty("y", UUID.randomUUID().toString()); // Generate random loot code
                    metaBlock.setProperty("$", "?");
                }
                break;
            // No valid item; do nothing
            default: break;
        }
    }
    
    private void fail(Player player, String reason) {
        player.notify(reason);
        Block block = player.getZone().getBlock(x, y);
        player.sendDelayedMessage(new BlockChangeMessage(x, y, layer, block.getItem(layer), block.getMod(layer)));
        player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
