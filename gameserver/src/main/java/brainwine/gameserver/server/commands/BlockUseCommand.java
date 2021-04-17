package brainwine.gameserver.server.commands;

import java.util.HashMap;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.msgpack.models.BlockUseData;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;
import brainwine.gameserver.zone.Zone;

@RegisterCommand(id = 21)
public class BlockUseCommand extends PlayerCommand {
    
    public int x;
    public int y;
    public Layer layer;
    public BlockUseData data;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        if(!player.isChunkActive(x, y)) {
            return;
        }
        
        if(data == null) {
            Item item = zone.getBlock(x, y).getItem(layer);
            int currentMod = zone.getBlock(x, y).getMod(layer);
            
            if(item.hasUse(ItemUseType.CHANGE)) {
                zone.updateBlock(x, y, layer, item, currentMod == 0 ? 1 : 0, player);
            }
        } else if(data.hasMetadata()) {
            // TODO
            Item item = zone.getBlock(x, y).getItem(layer);
            Map<String, Object> metadata = new HashMap<>();
            metadata.putAll(data.getMetadata());
            zone.setMetaBlock(x, y, item, player, metadata);
        } else if(data.hasPosition()) {
            int[] position = data.getPosition();
            int tX = position[0];
            int tY = position[1];
            Item item = zone.getBlock(tX, tY).getItem(layer);
            
            if(item.hasUse(ItemUseType.TELEPORT, ItemUseType.ZONE_TELEPORT)) {
                player.teleport(tX + 1, tY);
            }
        }
    }
}
