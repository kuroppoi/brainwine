package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.npc.behavior.ReactionEffect;
import brainwine.gameserver.entity.npc.behavior.Reactor;
import brainwine.gameserver.entity.npc.job.Job;
import brainwine.gameserver.player.Player;

public class DialoguerBehavior extends Behavior implements Reactor {
    
    private static final long NEXT_DIALOGUE_OVERALL_MS = 10_000;

    private long mostRecentDialogueAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000;

    public DialoguerBehavior(@JacksonInject Npc entity) {
        super(entity);
    }


    @Override
    public boolean behave() {
        if (System.currentTimeMillis() < mostRecentDialogueAt + NEXT_DIALOGUE_OVERALL_MS) {
            entity.setAnimation(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean react(Entity other, ReactionEffect message, Object params) {

        mostRecentDialogueAt = System.currentTimeMillis();

        message
            .onInteract(itemId -> {
                Job job = Job.get(entity.getJob());

                if (job != null) {
                    job.dialogue(entity, other);
                }        

                return;
            });

        return true;
    }

}
