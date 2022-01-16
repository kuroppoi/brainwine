package brainwine.gameserver.server.requests;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.loot.LootManager;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@SuppressWarnings("unchecked")
public class BlockUseRequest extends PlayerRequest {
    
    public int x;
    public int y;
    public Layer layer;
    
    @OptionalField
    public Object[] data;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        if(!player.isChunkActive(x, y)) {
            return;
        }
        
        if(data != null && data.length == 1 && data[0] instanceof Map) {
            data = ((Map<?, ?>)data[0]).values().toArray();
        }
        
        Block block = zone.getBlock(x, y);
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        Item item = block.getItem(layer);
        int mod = block.getMod(layer);
        
        if(metaBlock != null && item.hasUse(ItemUseType.PROTECTED)) {
            Player owner = GameServer.getInstance().getPlayerManager().getPlayerById(metaBlock.getOwner());
            
            if(player != owner) {
                if(item.hasUse(ItemUseType.PUBLIC)) {
                    String publicUse = item.getUse(ItemUseType.PUBLIC).toString();
                    
                    switch(publicUse) {
                        case "owner":
                            player.alert(String.format("This %s is owned by %s.", 
                                    item.getTitle().toLowerCase(), owner == null ? "nobody.." : owner.getName()));
                            break;
                    }
                } else {
                    player.alert("Sorry, that belongs to somebody else.");
                    return;
                }
            }
        }
        
        for(Entry<ItemUseType, Object> entry : item.getUses().entrySet()) {
            ItemUseType use = entry.getKey();
            Object value = entry.getValue();
            
            switch(use) {
            case DIALOG:
            case CREATE_DIALOG:
                if(data != null && value instanceof Map) {
                    Map<String, Object> config = (Map<String, Object>)value;
                    String target = MapHelper.getString(config, "target", "none");
                    
                    switch(target) {
                    case "meta":
                        Map<String, Object> metadata = new HashMap<>();
                        List<Map<String, Object>> sections = MapHelper.getList(config, "sections");
                        
                        if(sections != null && data.length == sections.size()) {
                            for(int i = 0; i < sections.size(); i++) {
                                Map<String, Object> section = sections.get(i);
                                String key = MapHelper.getString(section, "input.key");
                                
                                if(key != null) {
                                    metadata.put(key, data[i]);
                                } else if(MapHelper.getBoolean(section, "input.mod")) {
                                    List<Object> options = MapHelper.getList(section, "input.options");
                                    
                                    if(options != null) {
                                        mod = options.indexOf(data[i]);
                                        mod = mod == -1 ? 0 : mod;
                                        mod *= MapHelper.getInt(section, "input.mod_multiple", 1);
                                        zone.updateBlock(x, y, layer, item, mod, player);
                                    }
                                }
                            }
                        }
                        
                        // TODO find out what this is for
                        if(use == ItemUseType.CREATE_DIALOG) {
                            metadata.put("cd", true);
                        }
                        
                        zone.setMetaBlock(x, y, item, player, metadata);
                        break;
                    }
                }
                break;
            case CHANGE:
                zone.updateBlock(x, y, layer, item, mod == 0 ? 1 : 0, player);
                break;
            case CONTAINER:
                if(metaBlock != null) {
                    Map<String, Object> metadata = metaBlock.getMetadata();
                    String specialItem = MapHelper.getString(metadata, "$");
                    
                    if(specialItem != null) {
                        String dungeonId = MapHelper.getString(metadata, "@");
                        
                        if(dungeonId != null && item.hasUse(ItemUseType.FIELDABLE) && zone.isDungeonIntact(dungeonId)) {
                            player.alert("This container is secured by protectors in the area.");
                            break;
                        }
                                                
                        if(specialItem.equals("?")) {
                            metadata.remove("$");
                            LootManager lootManager = GameServer.getInstance().getLootManager();
                            Loot loot = lootManager.getRandomLoot(15, zone.getBiome(), item.getLootCategories()); // TODO level
                            
                            if(loot == null) {
                                player.alert("How quaint, this container is empty!");
                            } else {
                                player.awardLoot(loot, item.getLootGraphic());
                            }
                        } else {
                            player.alert("Sorry, this container can't be looted right now.");
                        }
                        
                        if(mod != 0) {
                            zone.updateBlock(x, y, Layer.FRONT, item, 0);
                        }
                    }
                }
                break;
            case TELEPORT:
                if(data != null && mod == 1 && data.length == 2 && data[0] instanceof Integer && data[1] instanceof Integer) {
                    int tX = (int)data[0];
                    int tY = (int)data[1];
                    MetaBlock target = zone.getMetaBlock(tX, tY);
                    
                    if(target != null && target.getItem().hasUse(ItemUseType.TELEPORT, ItemUseType.ZONE_TELEPORT)) {
                        player.teleport(tX + 1, tY);
                    }
                } else if(mod == 0) {
                    zone.updateBlock(x, y, layer, item, 1);
                    player.notify("You repaired a teleporter!", NotificationType.ACCOMPLISHMENT);
                    player.notifyPeers(String.format("%s repaired a teleporter.", player.getName()), NotificationType.SYSTEM);
                }
                break;
            case SWITCH:
                if(data == null) {
                    if(metaBlock != null) {
                        // TODO timed switches
                        
                        zone.updateBlock(x, y, layer, item, mod % 2 == 0 ? mod + 1 : mod - 1, player, null);
                        Map<String, Object> metadata = metaBlock.getMetadata();
                        List<List<Integer>> positions = MapHelper.getList(metadata, ">", Collections.emptyList());
                        
                        for(List<Integer> position : positions) {
                            int sX = position.get(0);
                            int sY = position.get(1);
                            Block target = zone.getBlock(sX, sY);
                            
                            if(target != null) {
                                Item switchedItem = target.getFrontItem();
                                
                                if(switchedItem.hasUse(ItemUseType.SWITCHED)) {
                                    if(!(item.getUse(ItemUseType.SWITCHED) instanceof String)) {
                                        int switchedMod = target.getFrontMod();
                                        zone.updateBlock(sX, sY, Layer.FRONT, switchedItem, switchedMod % 2 == 0 ? switchedMod + 1 : switchedMod - 1, null);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
            }
        }
    }
}
