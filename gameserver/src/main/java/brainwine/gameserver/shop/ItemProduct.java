package brainwine.gameserver.shop;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;

public class ItemProduct extends Product {
    
    private final Map<Item, Integer> items;
    
    @JsonCreator
    public ItemProduct(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "cost", required = true) int cost,
            @JsonProperty(value = "items", required = true) Map<Item, Integer> items) {
        super(name, cost);
        this.items = items;
    }
    
    @Override
    public void purchase(Player player) {
        DialogSection section = new DialogSection().setTitle("You received:");
        Dialog dialog = new Dialog().addSection(section);
        
        // Add items to inventory
        items.forEach((item, quantity) -> {
            if(quantity <= 0) {
                return;
            }
            
            section.addItem(new DialogListItem()
                    .setItem(item.getCode())
                    .setImage(String.format("inventory/%s", item.getId()))
                    .setText(String.format("%s x %s", item.getTitle(), quantity)));
            player.getInventory().addItem(item, quantity, true);
        });
        
        // Show dialog
        if(player.isV3()) {
            player.showDialog(dialog);
        } else {
            player.notify(dialog, NotificationType.REWARD);
        }
    }
    
    public Map<Item, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }
}
