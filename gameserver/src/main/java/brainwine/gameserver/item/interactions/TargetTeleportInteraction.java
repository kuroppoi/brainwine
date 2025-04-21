package brainwine.gameserver.item.interactions;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

import java.util.List;

public class TargetTeleportInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Do nothing if data is invalid
        if(data != null) {
            return;
        }
        
        Player player = (Player)entity;
        Zone targetZone = zone;
        String zoneName = metaBlock.getStringProperty("pz");
        
        // Validate target zone
        if(zoneName != null) {
            targetZone = GameServer.getInstance().getZoneManager().getZoneByName(zoneName);
            
            if(targetZone == null) {
                player.notify(String.format("Cannot locate world '%s', please recalibrate.", zoneName));
                return;
            }
            
            if(!targetZone.canJoin(player)) {
                player.notify("Sorry, you can't enter this world right now.");
                return;
            }
        }
        
        // Parse target position
        int targetX = -1;
        int targetY = metaBlock.getIntProperty("py") + (targetZone.getBiome() == Biome.DEEP ? -1000 : 200);
        int centerX = targetZone.getWidth() / 2;
        
        try {
            String strX = metaBlock.getStringProperty("px");
            
            if(strX != null) {
                if(strX.endsWith("w")) {
                    targetX = centerX - Integer.parseInt(strX.replace("w", ""));
                } else {
                    targetX = centerX + Integer.parseInt(strX.replace("e", ""));
                }
            }
        } catch(NumberFormatException e) {
            // Discard silently
        }
        
        // Do nothing if target is out of bounds
        if(!targetZone.areCoordinatesInBounds(targetX, targetY)) {
            player.notify("Cannot locate destination, please recalibrate.");
            return;
        }
        
        // Do nothing if target location is unexplored
        if(!player.isGodMode() && !targetZone.isAreaExplored(targetX, targetY)) {
            player.notify("That area hasn't been explored yet.");
            return;
        }

        List<MetaBlock> protectors = targetZone.getMetaBlocks(
                m -> m.hasOwner()
                               && m.getItem().getId().startsWith("mechanical/dish"));
        
        // Check area protection
        if(!player.isGodMode() && targetZone.isBlockProtectedByField(targetX, targetY, player, false, protectors)) {
            Player owner = metaBlock.getOwner();
            int setting = metaBlock.getIntProperty("pt");
            boolean ownerCanEdit = !targetZone.isBlockProtectedByField(targetX, targetY, owner, false, protectors);
            
            // Check protection entry setting
            if(owner == null || !ownerCanEdit || setting == 0 || (setting == 1 && !owner.isFollowing(player))) {
                player.notify("That area is protected.");
                return;
            }
        }
        
        // Teleport the player to the target location
        if(targetZone == zone) {
            player.teleport(targetX, targetY);
        } else {
            // Create confirmation dialog
            Dialog dialog = new Dialog()
                    .setActions("yesno")
                    .addSection(new DialogSection()
                            .setTitle("Attention")
                            .setText(String.format("Teleport to world '%s'?", targetZone.getName())));
            
            // Show confirmation dialog for zone change
            Zone _targetZone = targetZone;
            int _targetX = targetX;
            int _targetY = targetY;
            player.showDialog(dialog, input -> {
                // TODO figure out this v2 quirk
                if((!player.isV3() && input.length == 0) || (input.length == 1 && input[0].equals("Yes"))) {
                    player.changeZone(_targetZone, _targetX, _targetY);
                }
            });
        }
    }
}
