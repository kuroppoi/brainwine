package brainwine.gameserver.item.interactions;

import java.util.stream.Stream;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for lootable containers
 */
public class ContainerInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Check if the right data is present
        if(metaBlock == null || data != null) {
            return;
        }
        
        Player player = (Player)entity;
        String dungeonId = metaBlock.getStringProperty("@");
        
        // Check if container is protected by a dungeon
        if(item.hasUse(ItemUseType.FIELDABLE) && dungeonId != null && zone.isDungeonIntact(dungeonId)) {
            player.notify("This container is secured by protectors in the area.");
            return;
        }
        
        boolean plenty = item.hasUse(ItemUseType.PLENTY);
        String lootCode = metaBlock.getStringProperty("y");
        
        // Check loot code
        if(plenty) {
            if(lootCode == null) {
                player.notify("This chest cannot be plundered.");
                return;
            }
            
            if(player.hasLootCode(lootCode)) {
                player.notify("You've already plundered this chest.");
                return;
            }
        }
        
        String specialItem = metaBlock.getStringProperty("$");
        
        // Award loot
        if(specialItem != null) {
            if(specialItem.equals("?")) {
                Loot loot = metaBlock.hasProperty("l") ? new Loot(Item.get(metaBlock.getStringProperty("l")), metaBlock.getIntProperty("q"))
                        : GameServer.getInstance().getLootManager().getRandomLoot(player, item.getLootCategories());
                int experience = metaBlock.getIntProperty("xp");
                
                if(loot != null) {
                    if(plenty) {
                        player.addLootCode(lootCode);
                    } else {
                        metaBlock.removeProperty("$");
                        metaBlock.removeProperty("xp"); 
                    }
                    
                    player.awardLoot(loot, item.getLootGraphic());
                    player.addExperience(experience);
                    player.getStatistics().trackContainerLooted(item);
                } else {
                    player.notify("No eligible loot could be found for this container.");
                }
            } else {
                Item machinePart = ItemRegistry.getItem(specialItem);
                
                if(zone.addMachinePart(machinePart)) {
                    EcologicalMachine machine = EcologicalMachine.fromPart(machinePart);
                    String machineName = machine.toString().toLowerCase();
                    String determiner = Stream.of("a", "e", "i", "o", "u").filter(machineName::startsWith).findFirst().isPresent() ? "an" : "a";
                    String text = String.format("You discovered %s %s component!", determiner, machineName);
                    
                    if(player.isV3()) {
                        player.notify(text, NotificationType.ACCOMPLISHMENT);
                    } else {
                        Object message = MapHelper.map(String.class, String.class, 
                                "t", text,
                                "i", machinePart.getId());
                        player.notify(message, NotificationType.ACCOMPLISHMENT);
                    }
                    
                    player.notifyPeers(String.format("%s discovered %s %s component.", player.getName(), determiner, machineName), NotificationType.PEER_ACCOMPLISHMENT);
                    player.getStatistics().trackDiscovery(machinePart);
                    metaBlock.removeProperty("$");
                } else {
                    // TODO how should we handle this...?
                }
            }
        }
        
        // Update container mod
        if(!plenty && !metaBlock.hasProperty("$")) {
            zone.updateBlock(x, y, Layer.FRONT, item, 0, metaBlock.getOwner(), metaBlock.getMetadata());
        }
    }
}
