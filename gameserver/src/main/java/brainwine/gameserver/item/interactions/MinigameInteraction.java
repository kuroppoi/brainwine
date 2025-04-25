package brainwine.gameserver.item.interactions;

import java.util.Map;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.minigame.Minigame;
import brainwine.gameserver.minigame.Pandora;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@SuppressWarnings("unchecked")
public class MinigameInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Check item (to prevent dialog spoof)
        if(!zone.isChunkLoaded(x, y) || zone.getBlock(x, y).getFrontItem() != item) {
            return;
        }
        
        // Check & interact with active minigame
        // TODO verifying the type wouldn't be a bad idea
        Minigame activeMinigame = zone.getMinigame(x, y);
        Player player = (Player)entity;
        
        if(activeMinigame != null) {
            activeMinigame.onInteract(player);
            return;
        }
        
        // Do nothing if config is invalid
        if(!(config instanceof Map)) {
            return;
        }
        
        Map<String, Object> configMap = (Map<String, Object>)config;
        String type = MapHelper.getString(configMap, "type");
        
        // Check if type is present
        if(type == null) {
            player.notify("Minigame type has not been configured.");
            return;
        }
        
        // Handle custom minigame
        if(type.equals("custom")) {
            player.notify("Sorry, custom minigames are not supported yet.");
            return;
        }
        
        Map<String, Object> startDialog = MapHelper.getMap(configMap, "start_dialog");
        
        // Show start dialog if present and input hasn't been supplied
        if(startDialog != null && data == null) {
            player.showDialog(startDialog, input -> interact(zone, entity, x, y, layer, item, mod, metaBlock, config, input));
            return;
        } else if(startDialog != null && data.length == 1 && "cancel".equals(data[0])) {
            return; // Cancel action
        }
        
        // Check if max number of minigames has been reached
        if(zone.getMinigameCount() >= Zone.MAX_CONCURRENT_MINIGAMES) {
            player.notify("Sorry, the maximum number of active minigames has been reached.");
            return;
        }
        
        Minigame minigame = null;
        
        // Create minigame session based on type
        // TODO enum?
        switch(type) {
        case "pandora":
            minigame = new Pandora(zone, player, x, y);
            break;
        default:
            player.notify(String.format("Sorry, minigame type '%s' is not supported.", type));
            return;
        }
        
        // Let's get this party started!
        zone.startMinigame(minigame);
    }

}
