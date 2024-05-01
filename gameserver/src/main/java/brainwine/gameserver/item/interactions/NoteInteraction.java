package brainwine.gameserver.item.interactions;

import java.util.Collections;
import java.util.List;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public class NoteInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Do nothing if the right data isn't present
        if(metaBlock == null || data != null) {
            return;
        }
        
        Player player = (Player)entity;
        
        // Check if note contains a location
        if(metaBlock.hasProperty("l")) {
            List<Integer> location = MapHelper.getList(metaBlock.getMetadata(), "l", Collections.emptyList());
            String text = metaBlock.getStringProperty("t");
            
            // Do nothing if location data is invalid
            if(location.size() != 2) {
                return;
            }
            
            int locationX = location.get(0);
            int locationY = location.get(1);
            
            // Create dialog based on player version since v3 doesn't support map dialogs
            if(player.isV3()) {
                Dialog dialog = new Dialog()
                    .addSection(new DialogSection()
                            .setTitle("The note reads:")
                            .setText(text))
                    .addSection(new DialogSection()
                            .setText(zone.getReadableCoordinates(locationX, locationY)));
                player.showDialog(dialog);
            } else {
                // v2 dialog
                Dialog dialog = new Dialog()
                    .addSection(new DialogSection()
                            .setTitle(text))
                    .addSection(new DialogSection()
                            .setLocation(locationX, locationY));
                player.notify(dialog, NotificationType.NOTE);
            }
            
            return;
        }
        
        // Do nothing if player owns this note
        if(metaBlock.isOwnedBy(player)) {
            return;
        }
        
        // Build string from note segments
        String[] keys = { "t1", "t2", "t3", "t4", "t5", "t6" };
        StringBuilder builder = new StringBuilder();
        
        for(int i = 0; i < keys.length; i++) {
            String text = metaBlock.getStringProperty(keys[i]);
            
            // Skip if text is null or empty
            if(text == null || text.isEmpty()) {
                continue;
            }
            
            // Append space if necessary
            if(i > 0) {
                builder.append(" ");
            }
            
            builder.append(text);
        }
        
        // Show note text in dialog
        player.showDialog(DialogHelper.messageDialog("The note reads:", builder.toString()));
    }
}
