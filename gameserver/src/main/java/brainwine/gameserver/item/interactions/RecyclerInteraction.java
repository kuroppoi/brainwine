package brainwine.gameserver.item.interactions;

import java.util.Map;
import java.util.Map.Entry;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.DialogType;
import brainwine.gameserver.entity.player.Inventory;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for the recycler
 */
public class RecyclerInteraction extends EcologicalMachineInteraction {
    
    public RecyclerInteraction() {
        super(EcologicalMachine.RECYCLER);
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y) {
        Map<Item, Item> recyclables = MapHelper.map(Item.class, Item.class,
                ItemRegistry.getItem("rubble/iron"), ItemRegistry.getItem("building/iron"),
                ItemRegistry.getItem("rubble/copper"), ItemRegistry.getItem("building/copper"),
                ItemRegistry.getItem("rubble/brass"), ItemRegistry.getItem("building/brass"));
        int totalScrapRecycled = 0;
        int scrapRequired = (int)MathUtils.lerp(10.0, 5.0, player.getTotalSkillLevel(Skill.BUILDING) / 9.0);
        Inventory inventory = player.getInventory();
        DialogSection section = new DialogSection();
        
        // Recycle scrap
        for(Entry<Item, Item> entry : recyclables.entrySet()) {
            Item scrapItem = entry.getKey();
            int recycleCount = inventory.getQuantity(scrapItem) / scrapRequired;
            
            // Skip if there isn't enough scrap of this type to recycle
            if(recycleCount == 0) {
                continue;
            }
            
            // Update inventory
            Item resultItem = entry.getValue();
            int scrapCount = recycleCount * scrapRequired;
            totalScrapRecycled += scrapCount;
            inventory.removeItem(scrapItem, scrapCount, true);
            inventory.addItem(resultItem, recycleCount, true);
            
            // Create dialog item
            section.addItem(new DialogListItem()
                    .setItem(resultItem.getCode())
                    .setText(String.format("%s x %s", resultItem.getTitle(), recycleCount)));
        }
        
        // Check if anything was recycled
        if(totalScrapRecycled == 0) {
            player.notify("You do not have enough scrap to recycle.");
            return;
        }
        
        zone.spawnEffect(x + 2.0F, y, "area steam", 10);
        Dialog dialog = new Dialog()
                .addSection(section.setTitle(String.format("You recycled %s scrap into:", totalScrapRecycled)));
        
        // Show result dialog
        if(player.isV3()) {
            player.showDialog(dialog.setType(DialogType.LOOT));
        } else {
            player.notify(dialog, NotificationType.REWARD);
        }
    }
}
