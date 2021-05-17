package brainwine.gameserver.server.requests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.msgpack.models.BlockUseData;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Zone;

@SuppressWarnings("unchecked")
public class BlockUseRequest extends PlayerRequest {
    
    public int x;
    public int y;
    public Layer layer;
    
    @OptionalField
    public BlockUseData data;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        if(!player.isChunkActive(x, y)) {
            return;
        }
        
        Block block = zone.getBlock(x, y);
        Item item = block.getItem(layer);
        int mod = block.getMod(layer);
        
        if(data == null) {
            if(item.hasUse(ItemUseType.CHANGE)) {
                zone.updateBlock(x, y, layer, item, mod == 0 ? 1 : 0, player);
            }
        } else {
            Object[] data = this.data.getData();
            item.getUses().forEach((k, v) -> {
                switch(k) {
                case DIALOG:
                case CREATE_DIALOG:
                    // TODO rework dialog system and clean this mess up
                    Map<String, Object> config = (Map<String, Object>)v;
                    String target = MapHelper.getString(config, "target");
                    
                    switch(target) {
                    case "meta":
                        Map<String, Object> metadata = new HashMap<>();
                        List<Map<String, Object>> sections = MapHelper.getList(config, "sections");
                        int i = 0;
                        
                        for(Map<String, Object> section : sections) {
                            metadata.put(MapHelper.getString(section, "input.key"), data[i++]);
                        }
                        
                        zone.setMetaBlock(x, y, item, player, metadata);
                        break;
                    default:
                        break;
                    }
                    break;
                case TELEPORT:
                    if(mod == 1 && data.length == 2 && data[0] instanceof Integer && data[1] instanceof Integer) {
                        int tX = (int)data[0];
                        int tY = (int)data[1];
                        Block targetBlock = zone.getBlock(tX, tY);
                        
                        if(targetBlock != null) {
                            Item targetItem = targetBlock.getFrontItem();
                            
                            if(targetItem.hasUse(ItemUseType.TELEPORT, ItemUseType.ZONE_TELEPORT)) {
                                player.teleport(tX + 1, tY);
                            }
                        }
                    }
                    break;
                default:
                    break;
                }
            });
        }
        
        /*
        else if(data.hasMetadata()) {
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
        }*/
    }
}
