package brainwine.gameserver.item.interactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.messages.EffectMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for switches
 */
public class SwitchInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Player player, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if the required data isn't present
        if(data != null || metaBlock == null) {
            return;
        }
        
        int timer = metaBlock.getIntProperty("t");
        
        // Do nothing if this switch has a timer and is already flipped
        if(timer > 0 && mod % 2 == 1) {
            return;
        }
        
        // Show configured message to nearby players
        String message = metaBlock.getStringProperty("m");
        
        if(message != null && !message.isEmpty()) {
            float effectX = x + item.getBlockWidth() / 2.0F;
            float effectY = y - item.getBlockHeight() + 1;
            zone.sendMessageToChunk(new EffectMessage(effectX, effectY, "emote", message), zone.getChunk(x, y));
        }
        
        // Prepare list of targets
        List<List<Integer>> positions = MapHelper.getList(metaBlock.getMetadata(), ">", Collections.emptyList());
        List<Vector2i> targets = new ArrayList<>();
        targets.add(new Vector2i(x, y));
        positions.stream().map(position -> new Vector2i(position.get(0), position.get(1))).forEach(targets::add);
        int switchedMod = mod % 2 == 0 ? mod + 1 : mod - 1;
        
        // Switch all target blocks
        for(Vector2i target : targets) {
            switchBlock(zone, target.getX(), target.getY(), switchedMod);
        }
        
        // Create block timer if this is a timed switch
        if(timer > 0) {
            int unswitchedMod = switchedMod % 2 == 0 ? switchedMod + 1 : switchedMod - 1;
            
            zone.addBlockTimer(x, y, timer * 1000, () -> {
                for(Vector2i target : targets) {
                    switchBlock(zone, target.getX(), target.getY(), unswitchedMod);
                }
            });
        }
    }
    
    private void switchBlock(Zone zone, int x, int y, int mod) {
        // Do nothing if the target chunk isn't loaded
        if(!zone.isChunkLoaded(x, y)) {
            return;
        }
        
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        Player owner = metaBlock == null ? null : GameServer.getInstance().getPlayerManager().getPlayerById(metaBlock.getOwner());
        Map<String, Object> metadata = metaBlock == null ? null : metaBlock.getMetadata();
        Block block = zone.getBlock(x, y);
        Item item = block.getFrontItem();
        
        // Switch this block if it is a valid switch or door or something similar
        if(item.hasUse(ItemUseType.SWITCH) || (item.hasUse(ItemUseType.SWITCHED) && !(item.getUse(ItemUseType.SWITCHED) instanceof String))) {
            zone.updateBlock(x, y, Layer.FRONT, item, mod, owner, metadata);
        }
    }
}
