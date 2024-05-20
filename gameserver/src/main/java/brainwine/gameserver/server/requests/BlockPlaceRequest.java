package brainwine.gameserver.server.requests;

import java.util.UUID;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemGroup;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
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
        
        if(!player.isGodMode() && !MathUtils.inRange(x, y, player.getX(), player.getY(), player.getPlacementRange())) {
            fail(player, "This block is too far away.");
            return;
        }
        
        if(!player.isGodMode() && !player.getInventory().hasItem(item)) {
            fail(player, "You do not have enough of this item.");
            return;
        }
        
        if(!player.isGodMode() && !item.isPlacable()) {
            fail(player, "This item cannot be placed.");
            return;
        }
        
        if(!player.isGodMode() && item.getLayer() != layer) {
            fail(player, "This item cannot be placed here.");
            return;
        }
        
        if(!player.isGodMode() && !item.canPlaceInField() && zone.isBlockProtected(x, y, player)) {
            fail(player, "This block is protected.");
            return;
        }
        
        if(!player.isGodMode() && zone.isBlockOccupied(x, y, layer)) {
            fail(player, "This block is occupied.");
            return;
        }
        
        if(!player.isGodMode() && item.requiresPlacingSkill()) {
            Pair<Skill, Integer> placingSkill = item.getPlacingSkill();
            
            if(player.getTotalSkillLevel(placingSkill.getFirst()) < placingSkill.getLast()) {
                fail(player, "You are not skilled enough to place this block.");
                return;
            }
        }
        
        if(!player.isGodMode() && item.isDish() && zone.willDishOverlap(x, y, item.getField(), player)) {
            fail(player, "Dish will overlap another protector.");
            return;
        }
        
        if(layer == Layer.LIQUID) {
            mod = 5;
        } else if(item.getMod() == ModType.ROTATION && !item.isMirrorable()) {
            // Automatically orient rotatable blocks based on adjacent block
            if(zone.isChunkLoaded(x, y + 1) && zone.getBlock(x, y + 1).getFrontItem().isWhole()) {
                mod = 0;
            } else if(zone.isChunkLoaded(x, y - 1) && zone.getBlock(x, y - 1).getFrontItem().isWhole()) {
                mod = 2;
            } else if(zone.isChunkLoaded(x - 1, y) && zone.getBlock(x - 1, y).getFrontItem().isWhole()) {
                mod = 1;
            } else if(zone.isChunkLoaded(x + 1, y) && zone.getBlock(x + 1, y).getFrontItem().isWhole()) {
                mod = 3;
            }
        }
        
        zone.updateBlock(x, y, layer, item, mod, player);
        player.getInventory().removeItem(item);
        player.getStatistics().trackItemPlaced();
        player.trackPlacement(x, y, item);
        
        // Create block timer if applicable
        if(item.hasTimer()) {
            createBlockTimer(zone, player);
        }
        
        // Process custom place if applicable
        if(item.hasCustomPlace()) {
            processCustomPlace(zone, player);
        }
        
        // Misc processing
        if(item.getGroup() == ItemGroup.GRAVESTONE) {
            processBurial(zone, player);
        } else if(item.getGroup() == ItemGroup.CAGE) {
            processTrapping(zone, player);
        }
    }
    
    private void processTrapping(Zone zone, Player player) {
        // Check bounds
        if(x <= 0 || x + 1 >= zone.getWidth() || y <= 0 || y + 1 >= zone.getHeight()) {
            return;
        }
        
        // Do nothing if cage is not surrounded by whole blocks
        if(!zone.isBlockWhole(x - 1, y - 1) || !zone.isBlockWhole(x, y - 1) || !zone.isBlockWhole(x + 1, y - 1) || !zone.isBlockWhole(x - 1, y) || !zone.isBlockWhole(x + 1, y)
                || !zone.isBlockWhole(x - 1, y + 1) || !zone.isBlockWhole(x, y + 1) || !zone.isBlockWhole(x + 1, y + 1)) {
            return;
        }
        
        // Find random trappable entity at this location
        // TODO we have to do an isDead() check here because dead NPCs aren't always cleared immediately
        Npc entity = zone.getNpcs().stream()
                .filter(npc -> !npc.isDead() && !npc.isArtificial() && npc.getBlockX() == x && npc.getBlockY() == y && npc.getConfig().isTrappable())
                .findFirst().orElse(null);
        
        // Do nothing if no eligible entity was found
        if(entity == null) {
            return;
        }
        
        EntityConfig config = entity.getConfig();
        
        // Try to turn entity into a pet cage
        if(item.hasUse(ItemUseType.PET)) {
            // Don't waste it if entity has no pet variant
            if(!config.hasTrappablePetItem()) {
                return;
            }
            
            entity.setHealth(0.0F);
            zone.updateBlock(x, y, layer, 0);
            player.getInventory().addItem(config.getTrappablePetItem(), true);
            player.getStatistics().trackTrapping(config);
            return;
        }
        
        // Otherwise, kill the entity and place some fur
        // TODO v2 stores the quantity in the mod of "piled" items, but this functionality is not implemented here at all!
        entity.attack(player, item, entity.getHealth(), DamageType.ACID, true);
        zone.updateBlock(x, y, Layer.FRONT, "ground/fur");
        player.getStatistics().trackTrapping(config);
    }
    
    private void processBurial(Zone zone, Player player) {
        // Check bounds
        if(x <= 0 || x + 2 >= zone.getWidth() || y + 2 >= zone.getHeight()) {
            return;
        }
        
        // Do nothing if there is no skeleton underneath the gravestone
        if(!zone.getBlock(x, y + 1).getFrontItem().hasId("rubble/skeleton")) {
            return;
        }
        
        // Do nothing if the skeleton is obstructed
        if(zone.isBlockOccupied(x + 1, y + 1, Layer.FRONT)) {
            return;
        }
        
        // Do nothing if the skeleton isn't underground
        if(!zone.isUnderground(x, y + 1) || !zone.isUnderground(x + 1, y + 1)) {
            return;
        }
        
        // Do nothing if the gravestone isn't above ground
        if(zone.isUnderground(x, y) || zone.isUnderground(x + 1, y)) {
            return;
        }
        
        // Do nothing if the skeleton isn't surrounded by earth
        if(!zone.isBlockEarthy(x - 1, y + 1) || !zone.isBlockEarthy(x + 2, y + 1) || !zone.isBlockEarthy(x, y + 2) || !zone.isBlockEarthy(x + 1, y + 2)) {
            return;
        }
        
        // Everything checks out -- fill the grave!
        zone.updateBlock(x, y + 1, Layer.FRONT, "ground/earth");
        zone.updateBlock(x + 1, y + 1, Layer.FRONT, "ground/earth");
        zone.spawnEffect(x + 1.0F, y + 0.5F, "expiate", 20);
        zone.spawnEffect(x + 1.0F, y + 0.5F, "sparkle up", 20);
        
        // ~33% chance to spawn a ghost
        if(Math.random() < 0.334) {
            zone.spawnEntity("ghost", x + 1, y);
        }
        
        player.getStatistics().trackUndertaking();
    }
    
    private void createBlockTimer(Zone zone, Player player) {
        String type = item.getTimerType();
        int value = item.getTimerValue();
        Runnable task = null;
        
        switch(type) {
        case "front mod":
            task = () -> zone.updateBlock(x, y, layer, item, value);
            break;
        case "bomb":
            task = () -> zone.explode(x, y, value, player, true, value, DamageType.FIRE, value >= 6 ? "bomb-large" : "bomb");
            break;
        case "bomb-fire":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.FIRE, "bomb-fire");
            break;
        case "bomb-electric":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.ENERGY, "bomb-electric");
            break;
        case "bomb-frost":
            task = () -> zone.explode(x, y, value, player, false, value, DamageType.COLD, "bomb-frost");
            break;
        case "bomb-dig":
            task = () -> {
                zone.explode(x, y, value, player, "bomb-fire");
                int distance = value * 10;
                
                // Dig until we reach the maximum distance or hit a solid block
                for(int i = 1; i <= distance; i++) {
                    if(!zone.digBlock(x, y + i)) {
                        break;
                    }
                }
            };
            break;
        case "bomb-spawner":
            task = () -> {
                zone.explode(x, y, value, player, false, value, DamageType.FIRE, "bomb-fire");
                
                // Spawn a bunch of entities
                for(int i = 0; i < value; i++) {
                    zone.spawnEntity(item.getEntitySpawns().next(), x, y);
                }
            };
            break;
        case "bomb-water":
            task = () -> {
                zone.explode(x, y, value, player, false, value, DamageType.COLD, "bomb-large");
                zone.explodeLiquid(x, y, 4, "liquid/water");
            };
            break;
        case "bomb-acid":
            task = () -> {
                zone.explode(x, y, value, player, false, value, DamageType.ACID, "bomb-large");
                zone.explodeLiquid(x, y, 4, "liquid/acid");
            };
            break;
        case "bomb-lava":
            task = () -> {
                zone.explode(x, y, value, player, false, value, DamageType.FIRE, "bomb-large");
                zone.explodeLiquid(x, y, 4, "liquid/magma");
            };
            break;
        default:
            break;
        }
        
        if(task != null) {
            zone.addBlockTimer(x, y, item.getTimerDelay() * 1000, task);
        }
    }
    
    private void processCustomPlace(Zone zone, Player player) {        
        switch(item.getId()) {
            case "building/plug":
                // See if we can plug a maw or pipe
                Item baseItem = zone.getBlock(x, y).getBaseItem();
                String plugged = baseItem.hasId("base/maw") ? "base/maw-plugged"
                        : baseItem.hasId("base/pipe") ? "base/pipe-plugged" : null;
                
                if(plugged != null) {
                    zone.updateBlock(x, y, Layer.FRONT, 0); // Remove the plug front block
                    zone.updateBlock(x, y, Layer.BASE, plugged);
                    player.getStatistics().trackMawPlugged();
                }
                
                break;
            case "containers/chest-plenty":
            case "containers/sack-plenty":
                // Create additional metadata for chests o' plenty
                MetaBlock metaBlock = zone.getMetaBlock(x, y);
                
                if(metaBlock != null) {
                    metaBlock.setProperty("y", UUID.randomUUID().toString()); // Generate random loot code
                    metaBlock.setProperty("$", "?");
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
