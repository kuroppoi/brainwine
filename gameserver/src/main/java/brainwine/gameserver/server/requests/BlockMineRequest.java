package brainwine.gameserver.server.requests;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Action;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public class BlockMineRequest extends PlayerRequest {

    public int x;
    public int y;
    public Layer layer;
    public Item item;
    public int mod;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        boolean digging = item.isDiggable() && player.getHeldItem().getAction() == Action.DIG;
        
        if(!player.isChunkActive(x, y)) {
            player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        if(!MathUtils.inRange(x, y, player.getX(), player.getY(), player.getMiningRange()) && !player.isAdmin()) {
            fail(player, "This block is too far away.");
            return;
        }
        
        Block block = zone.getBlock(x, y);
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        
        if(block.getItem(layer) != item) {
            fail(player, "Could not find the item you're trying to mine.");
            return;
        }
        
        if(!digging && zone.isBlockProtected(x, y, player) && !player.isAdmin()) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(item.isInvulnerable() && !player.isAdmin()) {
            fail(player, "This block cannot be mined.");
            return;
        }
        
        if(digging) {
            zone.digBlock(x, y);
            return;
        }
        
        if(metaBlock != null) {
            Map<String, Object> metadata = metaBlock.getMetadata();
            
            if(!metaBlock.hasOwner() && item.hasUse(ItemUseType.SWITCH)) {
                List<List<Integer>> positions = MapHelper.getList(metadata, ">", Collections.emptyList());
                
                for(List<Integer> position : positions) {
                    Block target = zone.getBlock(position.get(0), position.get(1));
                    
                    if(target != null) {
                        Item switchedItem = target.getFrontItem();
                        
                        if(switchedItem.hasUse(ItemUseType.SWITCHED)) {
                            fail(player, String.format("This switch cannot be mined before its %s.", switchedItem.getTitle().toLowerCase()));
                            return;
                        }
                    }
                }
            }
            
            if(item.hasUse(ItemUseType.CONTAINER) && metadata.containsKey("$")) {
                fail(player, "Can't mine a container with loot in it.");
                return;
            }
            
            if(item.hasUse(ItemUseType.GUARD)) {
                String dungeonId = MapHelper.getString(metadata, "@");
                zone.destroyGuardBlock(dungeonId, player);
            }
        }
        
        Item inventoryItem = item.getMod() == ModType.DECAY && block.getMod(layer) > 0 ? item.getDecayInventoryItem() : item.getInventoryItem();
        zone.updateBlock(x, y, layer, 0, 0, player);
        
        if(!inventoryItem.isAir()) {
            player.getInventory().addItem(inventoryItem);
        }
    }
    
    private void fail(Player player, String reason) {
        player.alert(reason);
        Block block = player.getZone().getBlock(x, y);
        player.sendDelayedMessage(new BlockChangeMessage(x, y, layer, block.getItem(layer), block.getMod(layer)));
        player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
