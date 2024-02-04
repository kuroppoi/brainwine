package brainwine.gameserver.item.consumables;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.entity.player.Inventory;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.server.messages.InventoryMessage;

/**
 * Consumable handler for upgrade kits
 */
public class ConvertConsumable implements Consumable {

	@Override
	public void consume(Item item, Player player) {
		Map<Item, Item> conversions = item.getConversions();
		Inventory inventory = player.getInventory();
		
		// Find items in the player's inventory that can be upgraded
		Set<Item> convertables = conversions.keySet().stream().filter(i -> inventory.hasItem(i)).collect(Collectors.toSet());
		
		// Don't do anything if the player has no items that can be converted
		if(convertables.isEmpty()) {
			player.notify("You do not have any upgradeable items.");
			player.sendMessage(new InventoryMessage(inventory.getClientConfig(item)));
			return;
		}
		
		// Map item titles to their id
		Map<String, String> keyMap = convertables.stream().collect(Collectors.toMap(Item::getTitle, Item::getId));
		
		// Create upgrade dialog
		Dialog dialog = new Dialog().addSection(new DialogSection()
				.setTitle("Which item would you like to upgrade?")
				.setInput(new DialogSelectInput()
						.setOptions(convertables.stream().map(Item::getTitle).collect(Collectors.toList()))
						.setKey("item")));
		
		player.showDialog(dialog, data -> {
			// Fail if there is no data
			if(data.length == 0) {
				fail(item, player);
				return;
			}
			
			String key = keyMap.get(data[0]);
			
			// Fail if the chosen item title doesn't map to an id
			if(key == null) {
				fail(item, player);
				return;
			}
			
			Item itemToUpgrade = ItemRegistry.getItem(key);
			Item targetItem = conversions.get(itemToUpgrade);
			
			// Fail if the player doesn't have the item they want to upgrade or there is no upgrade for it
			if(!inventory.hasItem(itemToUpgrade) || targetItem == null) {
				fail(item, player);
				return;
			}
			
			inventory.removeItem(item, true); // Remove the consumable
			inventory.removeItem(itemToUpgrade, true); // Remove the item that was upgraded
			inventory.addItem(targetItem, true); // Add the item that the item upgraded to :)
			player.notify(String.format("%s upgraded to %s!", itemToUpgrade.getTitle(), targetItem.getTitle()));
		});
	}
	
	private void fail(Item item, Player player) {
		player.notify("Oops! There was a problem with the upgrade.");
		player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
	}
}
