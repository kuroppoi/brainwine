package brainwine.gameserver.server.requests;

import java.util.List;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Inventory;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.CraftingRequirement;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.MetaBlock;

/**
 * TODO Account for skills, bonuses etc..
 */
@RequestInfo(id = 19)
public class CraftRequest extends PlayerRequest {
    
    public Item item;
    
    @OptionalField
    public int quantity = 1;
    
    @Override
    public void process(Player player) {
        if(item.isAir() || !item.isCraftable()) {
            player.alert("Sorry, you can't craft this item.");
            return;
        }
        
        // Check if player has necessary ingredients
        List<CraftingRequirement> ingredients = item.getCraftingIngredients();
        Inventory inventory = player.getInventory();
        
        for(CraftingRequirement ingredient : ingredients) {
            Item item = ingredient.getItem();
            
            if(!inventory.hasItem(item, ingredient.getQuantity() * quantity)) {
                player.alert(String.format("You do not have enough %s to craft this.", item.getTitle()));
                return;
            }
        }
        
        // Check if required crafting helpers are nearby
        if(item.requiresWorkshop()) {
            List<MetaBlock> workshop = player.getZone().getMetaBlocks(metaBlock
                    -> MathUtils.inRange(player.getX(), player.getY(), metaBlock.getX(), metaBlock.getY(), 10));
            
            for(CraftingRequirement craftingHelper : item.getCraftingHelpers()) {
                int quantityMissing = craftingHelper.getQuantity() - (int)workshop.stream().filter(metaBlock
                        -> metaBlock.getItem() == craftingHelper.getItem()).count();
                
                if(quantityMissing > 0) {
                    player.alert(String.format("You can't craft this item because your workshop is lacking %sx %s.",
                            quantityMissing, craftingHelper.getItem().getTitle()));
                    return;
                }
            }
        }
        
        for(CraftingRequirement ingredient : ingredients) {
            inventory.removeItem(ingredient.getItem(), ingredient.getQuantity() * quantity);
        }
        
        inventory.addItem(item, item.getCraftingQuantity() * quantity);
    }
}
