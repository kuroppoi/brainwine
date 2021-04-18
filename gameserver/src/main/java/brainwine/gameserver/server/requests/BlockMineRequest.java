package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Block;
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
        
        if(!player.isChunkActive(x, y)) {
            player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        if(!MathUtils.inRange(x, y, player.getX(), player.getY(), player.getMiningRange()) && !player.isAdmin()) {
            fail(player, "This block is too far away.");
            return;
        }
        
        Block block = zone.getBlock(x, y);
        
        if(block.getItem(layer) != item && block.getMod(layer) != mod) {
            fail(player, "Could not find the item you're trying to mine.");
            return;
        }
        
        if(zone.isBlockProtected(x, y, player)) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(item.isInvulnerable() && !player.isAdmin()) {
            fail(player, "This block cannot be mined.");
            return;
        }
        
        zone.updateBlock(x, y, layer, 0, 0, player);
    }
    
    private void fail(Player player, String reason) {
        player.alert(reason);
        Block block = player.getZone().getBlock(x, y);
        player.sendDelayedMessage(new BlockChangeMessage(x, y, layer, block.getItem(layer), block.getMod(layer)));
        player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
