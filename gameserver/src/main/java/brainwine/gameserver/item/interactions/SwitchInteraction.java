package brainwine.gameserver.item.interactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for switches
 */
public class SwitchInteraction implements ItemInteraction {
    
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
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
            zone.spawnEffect(effectX, effectY, "emote", message);
        }
        
        // Prepare list of targets
        List<List<Integer>> positions = MapHelper.getList(metaBlock.getMetadata(), ">", Collections.emptyList());
        List<Vector2i> targets = new ArrayList<>();
        targets.add(new Vector2i(x, y));
        positions.stream().map(position -> new Vector2i(position.get(0), position.get(1))).forEach(targets::add);
        int switchedMod = mod % 2 == 0 ? mod + 1 : mod - 1;
        
        // Switch all target blocks
        for(Vector2i target : targets) {
            switchBlock(zone, entity, target.getX(), target.getY(), switchedMod, metaBlock);
        }
        
        // Create block timer if this is a timed switch
        if(timer > 0) {
            int unswitchedMod = switchedMod % 2 == 0 ? switchedMod + 1 : switchedMod - 1;
            
            zone.addBlockTimer(x, y, timer * 1000, () -> {
                for(Vector2i target : targets) {
                    switchBlock(zone, entity, target.getX(), target.getY(), unswitchedMod, metaBlock);
                }
            });
        }
    }
    
    private void switchBlock(Zone zone, Entity entity, int x, int y, int mod, MetaBlock switchMeta) {
        // Do nothing if the target chunk isn't loaded
        if(!zone.isChunkLoaded(x, y)) {
            return;
        }
        
        MetaBlock metaBlock = zone.getMetaBlock(x, y);
        
        // Do nothing if there is no metadata
        if(metaBlock == null) {
            return;
        }
        
        Player owner = metaBlock == null ? null : metaBlock.getOwner();
        Map<String, Object> metadata = metaBlock == null ? null : metaBlock.getMetadata();
        Item item = metaBlock.getItem();
        Object config = item.getUse(ItemUseType.SWITCHED);
        
        if(config instanceof String) {
            String type = (String)config;
            
            // Not the prettiest way to do this but it will have to do.
            switch(type.toLowerCase()) {
            case "spawner": switchSpawner(zone, metaBlock); break;
            case "exploder": switchExploder(zone, entity, metaBlock); break;
            case "messagesign": switchSign(zone, entity, metaBlock, switchMeta); break;
            default: break;
            }
        } else if(item.hasUse(ItemUseType.SWITCH, ItemUseType.SWITCHED, ItemUseType.TRIGGER)) {
            zone.updateBlock(x, y, Layer.FRONT, item, mod, owner, metadata);
        }
    }
    
    private void switchSpawner(Zone zone, MetaBlock metaBlock) {
        // Kill existing entity
        if(metaBlock.hasProperty("eid")) {
            Entity entity = zone.getEntity(metaBlock.getIntProperty("eid"));
            
            if(entity instanceof Npc) {
                Npc npc = (Npc)entity;
                
                // TODO an isArtificial() check will work well enough as a fix for #45 for now since spawners are the only things that use it
                if(!npc.isDead() && npc.isArtificial()) {
                    npc.spawnEffect("bomb-teleport", 4);
                    npc.setHealth(0);
                }
            }
        }
        
        Object config = metaBlock.getItem().getUse(ItemUseType.SPAWN);
        
        // Do nothing if use config data is invalid
        if(!(config instanceof Map)) {
            return;
        }
        
        // Try to spawn entity
        String entityType = MapHelper.getString((Map<?, ?>)config, metaBlock.getStringProperty("e"));
        Npc npc = zone.spawnEntity(entityType, metaBlock.getX(), metaBlock.getY(), true);
        
        // Do nothing if entity failed to spawn
        if(npc == null) {
            return;
        }
        
        npc.setArtificial(true);
        metaBlock.setProperty("eid", npc.getId());
    }
    
    // TODO exploders were used to create lag machines back in the day, so maybe we should put a cooldown on this
    private void switchExploder(Zone zone, Entity entity, MetaBlock metaBlock) {
        String type = metaBlock.getStringProperty("e");
        
        // Do nothing if explosion type doesn't exist in block metadata
        if(type == null) {
            return;
        }
        
        int x = metaBlock.getX();
        int y = metaBlock.getY();
        
        // Do nothing if exploder isn't activated
        if(zone.getBlock(x, y).getFrontMod() == 0) {
            return;
        }
        
        // Create explosion
        DamageType damageType = type.equalsIgnoreCase("electric") ? DamageType.ENERGY : DamageType.fromName(type);
        String effect = String.format("bomb-%s", type.toLowerCase());
        zone.explode(x, y, 6, entity, false, 6, damageType, effect);
    }
    
    private void switchSign(Zone zone, Entity entity, MetaBlock metaBlock, MetaBlock switchMeta) {
        String message = switchMeta.hasProperty("m") ? switchMeta.getStringProperty("m").trim() : "";
        boolean lock = metaBlock.hasProperty("lock") && metaBlock.getStringProperty("lock").equalsIgnoreCase("yes");
        Item item = metaBlock.getItem();
        
        // Check and update lock status
        if(lock) {
            boolean locked = metaBlock.getBooleanProperty("locked");
            
            if(!message.isEmpty()) {
                if(locked) {
                    return;
                }
                
                metaBlock.setProperty("locked", true);
            } else if(locked) {
                metaBlock.removeProperty("locked");
            }
        }
        
        // Update sign text
        String name = entity.getName();
        
        if(name != null) {
            message = message.replaceAll("%t%", name);
        }
        
        String separator = "\n";
        String[] keys = {"t1", "t2", "t3", "t4"};
        String[] segments = WordUtils.wrap(message, 20, separator, true).split(separator, 4);
       
        for(int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String text = i < segments.length ? segments[i] : "";
            int separatorIndex = text.lastIndexOf(separator);
            
            if(separatorIndex != -1) {
                text = text.substring(0, separatorIndex);
            }
            
            metaBlock.setProperty(key, text);
        }
        
        // Send data to players
        float effectX = metaBlock.getX() + (float)item.getBlockWidth() / 2;
        float effectY = metaBlock.getY() - (float)item.getBlockHeight() / 2 + 1;
        zone.spawnEffect(effectX, effectY, "area steam", 10);
        zone.sendBlockMetaUpdate(metaBlock);
    }
}
