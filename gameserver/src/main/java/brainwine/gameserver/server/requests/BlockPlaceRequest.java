package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.Block;
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
        
        if(!MathUtils.inRange(x, y, player.getX(), player.getY(), player.getPlacementRange()) && !player.isAdmin()) {
            fail(player, "This block is too far away.");
            return;
        }
        
        if(!player.getInventory().hasItem(item)) {
            fail(player, "You do not have enough of this item.");
            return;
        }
        
        if(!item.isPlacable() && !player.isAdmin()) {
            fail(player, "This item cannot be placed.");
            return;
        }
        
        if(item.getLayer() != layer && !player.isAdmin()) {
            fail(player, "This item cannot be placed here.");
            return;
        }
        
        if(zone.isBlockProtected(x, y, player) && !player.isAdmin()) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(zone.isBlockOccupied(x, y, layer) && !player.isAdmin()) {
            fail(player, "This block is occupied.");
            return;
        }
        
        if(item.requiresPlacingSkill()) {
            Pair<Skill, Integer> placingSkill = item.getPlacingSkill();
            
            if(player.getTotalSkillLevel(placingSkill.getFirst()) < placingSkill.getLast()) {
                fail(player, "You are not skilled enough to place this block.");
                return;
            }
        }
        
        if(item.isDish() && zone.willDishOverlap(x, y, item.getField(), player)) {
            fail(player, "Dish will overlap another protector.");
            return;
        }
        
        if(layer == Layer.LIQUID) {
            mod = 5;
        }
        
        zone.updateBlock(x, y, layer, item, mod, player);
        player.getInventory().removeItem(item);
        player.getStatistics().trackItemPlaced();
        player.trackPlacement(x, y, item);
        
        // Process custom place if applicable
        if(item.hasCustomPlace()) {
            processCustomPlace(player);
        }
    }
    
    private void processCustomPlace(Player player) {
        Zone zone = player.getZone();
        
        switch(item.getId()) {
            // See if we can plug a maw or pipe
            case "building/plug":
                Item baseItem = zone.getBlock(x, y).getBaseItem();
                String plugged = baseItem.hasId("base/maw") ? "base/maw-plugged"
                        : baseItem.hasId("base/pipe") ? "base/pipe-plugged" : null;
                
                if(plugged != null) {
                    zone.updateBlock(x, y, Layer.FRONT, 0); // Remove the plug front block
                    zone.updateBlock(x, y, Layer.BASE, plugged);
                    player.getStatistics().trackMawPlugged();
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
