package brainwine.gameserver.server.requests;

import java.util.List;
import java.util.stream.Collectors;

import brainwine.gameserver.item.CraftingRequirement;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Inventory;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 19)
public class CraftRequest extends PlayerRequest {
    
    public Item item;
    
    @OptionalField
    public int quantity = 1;
    
    @Override
    public void process(Player player) {
        if(item.isAir() || !item.isCraftable()) {
            player.notify("Sorry, you can't craft this item.");
            return;
        }
        
        // Check crafting skill
        if(!player.isGodMode() && item.requiresCraftingSkill()) {
            Pair<Skill, Integer> craftingSkill = item.getCraftingSkill();
            
            if(player.getTotalSkillLevel(craftingSkill.getFirst()) < craftingSkill.getLast()) {
                player.notify("You are not skilled enough to craft this item.");
                return;
            }
        }
        
        // Check if player has necessary ingredients
        List<CraftingRequirement> ingredients = item.getCraftingIngredients();
        Inventory inventory = player.getInventory();
        
        for(CraftingRequirement ingredient : ingredients) {
            Item item = ingredient.getItem();
            
            if(!inventory.hasItem(item, ingredient.getQuantity() * quantity)) {
                player.notify(String.format("You do not have enough %s to craft this.", item.getTitle()));
                return;
            }
        }

        // Check if required crafting helpers are nearby
        if(!player.isGodMode() && item.requiresWorkshop()) {
            Zone zone = player.getZone();
            
            // Fetch list of all meta blocks in the player's vicinity
            List<MetaBlock> workshop = zone.getMetaBlocks(metaBlock -> zone.isChunkLoaded(metaBlock.getX(), metaBlock.getY())
                    && MathUtils.inRange(player.getX(), player.getY(), metaBlock.getX(), metaBlock.getY(), 20));
            
            // Check for each crafting helper if it is present in the workshop and available for use
            for(CraftingRequirement craftingHelper : item.getCraftingHelpers()) {
                int quantityRequired = craftingHelper.getQuantity();
                
                // Fetch list of crafting helpers of this type that are present in the workshop
                List<MetaBlock> presentCraftingHelpers = workshop.stream()
                        .filter(metaBlock -> metaBlock.getItem() == craftingHelper.getItem()).collect(Collectors.toList());
                int quantityMissing = quantityRequired - presentCraftingHelpers.size();
                
                // Check if workshop is still missing crafting helpers of this type and notify the player if this is the case
                if(quantityMissing > 0) {
                    player.notify(String.format("You can't craft this item because your workshop is still lacking %sx %s.",
                            quantityMissing, craftingHelper.getItem().getTitle()));
                    return;
                }

                // Perform additional checks if the crafting helper requires steam to function
                if(craftingHelper.getItem().usesSteam()) {
                    quantityMissing = quantityRequired - (int)presentCraftingHelpers.stream()
                            .filter(metaBlock -> zone.getBlock(metaBlock.getX(), metaBlock.getY()).getFrontMod() == 1).count();
                    
                    // Notify the player if not enough crafting helpers are powered
                    if(quantityMissing > 0) {
                        player.notify(String.format("You can't craft this item because your workshop still needs to provide steam power to %sx %s.",
                                quantityMissing, craftingHelper.getItem().getTitle()));
                        return;
                    }
                }
            }
        }
        
        for(CraftingRequirement ingredient : ingredients) {
            inventory.removeItem(ingredient.getItem(), ingredient.getQuantity() * quantity);
        }
        
        int totalQuantity = item.getCraftingQuantity() * quantity;
        inventory.addItem(item, totalQuantity);
        player.getStatistics().trackItemCrafted(item, totalQuantity);
    }
}
