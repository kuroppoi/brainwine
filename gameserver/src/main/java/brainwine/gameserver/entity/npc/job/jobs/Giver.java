package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.Job;
import brainwine.gameserver.player.Player;

public class Giver extends Job {

    @Override
    public boolean dialogue(Npc me, Player player) {
        return true;
    }
    
}
