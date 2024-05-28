package brainwine.gameserver.entity.npc.behavior;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.composed.CrawlerBehavior;
import brainwine.gameserver.entity.npc.behavior.composed.DiggerBehavior;
import brainwine.gameserver.entity.npc.behavior.composed.FlyerBehavior;
import brainwine.gameserver.entity.npc.behavior.composed.WalkerBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ClimbBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ConveyorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.DigBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.EruptionAttackBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FallBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FlyBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FlyTowardBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FollowBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.IdleBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.RandomlyTargetBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ReporterBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ShielderBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.SpawnAttackBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.TurnBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.UnblockBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.WalkBehavior;

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
    @Type(name = "conveyor", value = ConveyorBehavior.class),
    @Type(name = "follow", value = FollowBehavior.class),
    @Type(name = "climb", value = ClimbBehavior.class),
    @Type(name = "dig", value = DigBehavior.class),
    @Type(name = "fly", value = FlyBehavior.class),
    @Type(name = "fly_toward", value = FlyTowardBehavior.class),
    @Type(name = "shielder", value = ShielderBehavior.class),
    @Type(name = "spawn_attack", value = SpawnAttackBehavior.class),
    @Type(name = "eruption_attack", value = EruptionAttackBehavior.class),
    @Type(name = "randomly_target", value = RandomlyTargetBehavior.class),
    @Type(name = "reporter", value = ReporterBehavior.class),
    @Type(name = "unblock", value = UnblockBehavior.class)
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
