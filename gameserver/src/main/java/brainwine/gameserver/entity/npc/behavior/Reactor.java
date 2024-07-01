package brainwine.gameserver.entity.npc.behavior;

import brainwine.gameserver.entity.Entity;

public interface Reactor {
    /**Return true if you have captured the effect, return false otherwise so any reactors that come next get a chance to react also.
     * 
     * @param entity the other entity which has effected our reactor
     * @param message the type of effect
     * @param params depends on what kind of interaction this is, usually a list of objects.
     */
    boolean react(Entity other, ReactionEffect message, Object params);
}
