package brainwine.gameserver.item.interactions;

import java.util.Map;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for items that explode if you get too close
 */
@SuppressWarnings("unchecked")
public class BurstInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        // Do nothing if data is invalid
        if(!(config instanceof Map)) {
            return;
        }
        
        Player player = (Player)entity;
        Map<String, Object> configMap = (Map<String, Object>)config;
        boolean dodge = MapHelper.getBoolean(configMap, "dodge");

        // Do nothing if the player is lucky enough :)
        if(dodge && Math.random() * Player.MAX_SKILL_LEVEL <= player.getTotalSkillLevel(Skill.AGILITY) / 2.0F) {
            return;
        }

        boolean natural = MapHelper.getBoolean(configMap, "natural");
        boolean enemy = MapHelper.getBoolean(configMap, "enemy");
        Block block = zone.getBlock(x, y);
        
        // Check if the block has to be be natural or triggered by an enemy
        if((natural && !block.isNatural()) || (enemy && (player.isStealthy() || block.getOwnerHash() == player.getBlockHash()))) {
            return;
        }

        DamageType damageType = DamageType.fromName(MapHelper.getString(configMap, "damage_type"));
        String effect = MapHelper.getString(configMap, "effect", "bomb");
        float range = MapHelper.getFloat(configMap, "range");
        float damage = MapHelper.getFloat(configMap, "damage");
        boolean destructive = MapHelper.getBoolean(configMap, "destructive");
        
        // Create explosion and destroy block
        zone.explode(x, y, range, null, destructive, damage, damageType, effect);
        zone.updateBlock(x, y, layer, 0);
    }
}
