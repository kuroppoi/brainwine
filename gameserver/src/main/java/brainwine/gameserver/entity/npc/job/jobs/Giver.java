package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.Job;

public class Giver extends Job {

    @Override
    public boolean dialogue(Npc me, Entity other) {
        return true;
    }
    
}
