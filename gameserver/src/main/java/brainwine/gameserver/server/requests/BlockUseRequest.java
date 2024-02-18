package brainwine.gameserver.server.requests;

import java.util.Map;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.interactions.ItemInteraction;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 21)
public class BlockUseRequest extends PlayerRequest {
    
    public int x;
    public int y;
    public Layer layer;
    
    @OptionalField
    public Object[] data;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        // Do nothing if player is dead or if the target chunk is not active
        if(player.isDead() || !player.isChunkActive(x, y)) {
            return;
        }
        
        // Do nothing if player is too far away
        if(!player.isGodMode() && !player.inRange(x, y, player.getMiningRange())) {
            return;
        }
        
        // Transform usage data if necessary
        if(data != null && data.length == 1 && data[0] instanceof Map) {
            data = ((Map<?, ?>)data[0]).values().toArray();
        }
        
        Block block = zone.getBlock(x, y);
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        Item item = block.getItem(layer);
        int mod = block.getMod(layer);
        
        // Check if block is owned by another player
        if(metaBlock != null && item.hasUse(ItemUseType.PROTECTED)) {
            Player owner = metaBlock.getOwner();
            
            if(player != owner) {
                if(item.hasUse(ItemUseType.PUBLIC)) {
                    String publicUse = item.getUse(ItemUseType.PUBLIC).toString();
                    
                    // TODO implement other cases
                    switch(publicUse) {
                    case "owner":
                        player.notify(String.format("This %s is owned by %s.", 
                                item.getTitle().toLowerCase(), owner == null ? "somebody else" : owner.getName()));
                        break;
                    case "note":
                        ItemUseType.NOTE.getInteraction().interact(zone, player, x, y, layer, item, mod, metaBlock, null, data);
                        break;
                    default: break;
                    }
                } else {
                    player.notify("Sorry, that belongs to somebody else.");
                }
                
                return;
            }
        }
        
        // Try to interact with the block
        item.getUses().forEach((use, config) -> {
            ItemInteraction interaction = use.getInteraction();
            
            if(interaction != null) {
                interaction.interact(zone, player, x, y, layer, item, mod, metaBlock, config, data);
            }
        });
    }
}
