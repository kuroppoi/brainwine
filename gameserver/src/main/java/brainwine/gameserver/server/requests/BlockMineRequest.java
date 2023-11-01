package brainwine.gameserver.server.requests;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.item.Action;
import brainwine.gameserver.item.Fieldability;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.MiningBonus;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 11)
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
        
        if(player.isDead()) {
            return;
        }
        
        if(!player.isChunkActive(x, y)) {
            player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
            return;
        }
        
        if(!player.isGodMode() && !MathUtils.inRange(x, y, player.getX(), player.getY(), player.getMiningRange())) {
            fail(player, "This block is too far away.");
            return;
        }
        
        Block block = zone.getBlock(x, y);
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        
        if(block.getItem(layer) != item) {
            fail(player, "Could not find the item you're trying to mine.");
            return;
        }
        
        // TODO block ownership & 'placed' fieldability
        if(!player.isGodMode() && !digging && item.getFieldability() == Fieldability.TRUE && zone.isBlockProtected(x, y, player)) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(!player.isGodMode() && item.isInvulnerable()) {
            fail(player, "This block cannot be mined.");
            return;
        }
        
        if(!player.isGodMode() && item.isEntity()) {
            fail(player, "You must destroy the entity instead of its mount.");
            return;
        }
        
        if(!player.isGodMode() && item.requiresMiningSkill()) {
            Pair<Skill, Integer> miningSkill = item.getMiningSkill();
            
            if(player.getTotalSkillLevel(miningSkill.getFirst()) < miningSkill.getLast()) {
                fail(player, "You are not skilled enough to mine this block.");
                return;
            }
        }
        
        if(digging) {
            zone.digBlock(x, y);
            return;
        }
        
        if(metaBlock != null) {
            Map<String, Object> metadata = metaBlock.getMetadata();
            
            // Check if block is a natural switch with an active door
            if(!player.isGodMode() && !metaBlock.hasOwner() && item.hasUse(ItemUseType.SWITCH)) {
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
            
            // Check if block is an unlooted container
            if(!player.isGodMode() && item.hasUse(ItemUseType.CONTAINER) && metadata.containsKey("$")) {
                fail(player, "Can't mine a container with loot in it.");
                return;
            }
            
            // Unindex guard block if it is one
            if(item.hasUse(ItemUseType.GUARD)) {
                String dungeonId = MapHelper.getString(metadata, "@");
                zone.destroyGuardBlock(dungeonId, player);
            }
        }
        
        zone.updateBlock(x, y, layer, 0, 0, player);
        player.getStatistics().trackItemMined(item);
        Item inventoryItem = item.getMod() == ModType.DECAY && block.getMod(layer) > 0 ? item.getDecayInventoryItem() : item.getInventoryItem();
        int quantity = 1;
        
        // Apply mining bonus if there is one
        if(item.hasMiningBonus()) {
            MiningBonus bonus = item.getMiningBonus();
            if(Math.random() < player.getMiningBonusChance(bonus)) {
                if(!bonus.getItem().isAir()) {
                    inventoryItem = bonus.getItem();
                }
                                
                if(bonus.isDoubleLoot()) {
                    quantity *= 2;
                }
                
                player.notify(bonus.getNotification(), NotificationType.FANCY_EMOTE);
            }
        }
        
        if(!inventoryItem.isAir()) {
            player.getInventory().addItem(inventoryItem, quantity, true);
        }
    }
    
    private void fail(Player player, String reason) {
        player.notify(reason);
        Block block = player.getZone().getBlock(x, y);
        player.sendDelayedMessage(new BlockChangeMessage(x, y, layer, block.getItem(layer), block.getMod(layer)));
        player.sendDelayedMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
