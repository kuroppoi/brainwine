package brainwine.gameserver.entity.npc.job;

import java.util.Map;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.jobs.*;

public abstract class Job {
    private static Map<String, Job> jobMap = Map.of(
        "giver", new Giver(),
        "joker", new Joker()
    );

    private static Job defaultJob = new Joker();

    public abstract boolean dialogue(Npc me, Entity other);

    public static Job get(String type) {
        if (type != null) {
            return jobMap.getOrDefault(type, defaultJob);
        } else {
            return defaultJob;
        }
    }
}
