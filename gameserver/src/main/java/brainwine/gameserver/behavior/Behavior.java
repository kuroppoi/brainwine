package brainwine.gameserver.behavior;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import brainwine.gameserver.behavior.composed.CrawlerBehavior;
import brainwine.gameserver.behavior.composed.DiggerBehavior;
import brainwine.gameserver.behavior.composed.FlyerBehavior;
import brainwine.gameserver.behavior.composed.WalkerBehavior;
import brainwine.gameserver.behavior.parts.ClimbBehavior;
import brainwine.gameserver.behavior.parts.DigBehavior;
import brainwine.gameserver.behavior.parts.FallBehavior;
import brainwine.gameserver.behavior.parts.FlyBehavior;
import brainwine.gameserver.behavior.parts.FlyTowardBehavior;
import brainwine.gameserver.behavior.parts.FollowBehavior;
import brainwine.gameserver.behavior.parts.IdleBehavior;
import brainwine.gameserver.behavior.parts.RandomlyTargetBehavior;
import brainwine.gameserver.behavior.parts.ShielderBehavior;
import brainwine.gameserver.behavior.parts.SpawnAttackBehavior;
import brainwine.gameserver.behavior.parts.TurnBehavior;
import brainwine.gameserver.behavior.parts.WalkBehavior;
import brainwine.gameserver.entity.npc.Npc;

/**
 * Heavily based on Deepworld's original "rubyhave" (ha ha very punny) behavior system.
 * 
 * https://github.com/bytebin/deepworld-gameserver/tree/master/vendor/rubyhave
 * https://github.com/bytebin/deepworld-gameserver/tree/master/models/npcs/behavior
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    // Composed
    @Type(name = "walker", value = WalkerBehavior.class),
    @Type(name = "crawler", value = CrawlerBehavior.class),
    @Type(name = "flyer", value = FlyerBehavior.class),
    @Type(name = "digger", value = DiggerBehavior.class),
    // Parts
    @Type(name = "idle", value = IdleBehavior.class),
    @Type(name = "walk", value = WalkBehavior.class),
    @Type(name = "fall", value = FallBehavior.class),
    @Type(name = "turn", value = TurnBehavior.class),
    @Type(name = "follow", value = FollowBehavior.class),
    @Type(name = "climb", value = ClimbBehavior.class),
    @Type(name = "dig", value = DigBehavior.class),
    @Type(name = "fly", value = FlyBehavior.class),
    @Type(name = "fly_toward", value = FlyTowardBehavior.class),
    @Type(name = "shielder", value = ShielderBehavior.class),
    @Type(name = "spawn_attack", value = SpawnAttackBehavior.class),
    @Type(name = "randomly_target", value = RandomlyTargetBehavior.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Behavior {
    
    protected final Npc entity;
    
    public Behavior(Npc entity) {
        this.entity = entity;
    }
    
    public abstract boolean behave();
    
    public boolean canBehave() {
        return true;
    }
}
