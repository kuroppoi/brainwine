package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Zone;

@RegisterCommand(id = 12)
public class BlockPlaceCommand extends PlayerCommand {

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
		
		if(item.isDish() && zone.willDishOverlap(x, y, item.getField(), player)) {
			fail(player, "Dish will overlap another protector.");
			return;
		}
		
		if(layer == Layer.LIQUID) {
			mod = 5;
		}
		
		zone.updateBlock(x, y, layer, item, mod, player);
	}
	
	private void fail(Player player, String reason) {
		player.alert(reason);
		Block block = player.getZone().getBlock(x, y);
		player.sendDelayedMessage(new BlockChangeMessage(x, y, layer, block.getItem(layer), block.getMod(layer)));
		player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
	}
}
