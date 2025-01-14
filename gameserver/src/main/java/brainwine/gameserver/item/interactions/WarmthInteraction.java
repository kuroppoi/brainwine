package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for blocks that provide warmth
 */
public class WarmthInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Do nothing if object isn't lit or active
        if(mod == 0) {
            return;
        }
        
        Player player = (Player)entity;
        player.applyWarmth();
        player.notify("Ahh, toasty warmth.");
    }
}
