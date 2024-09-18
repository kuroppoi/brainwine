package brainwine.gameserver.entity.npc.job;

import java.util.Map;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.jobs.*;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;

public abstract class Job {
    private static Map<String, Job> jobMap = MapHelper.mapOf(
        "giver", new Giver(),
        "joker", new Joker(),
        "crafter", new Crafter()
    );

    private static Job defaultJob = new Joker();

    public abstract boolean dialogue(Npc me, Player player);

    public static boolean validateJob(String type) {
        return jobMap.containsKey(type);
    }

    public static Job get(String type) {
        if (type != null) {
            return jobMap.getOrDefault(type, defaultJob);
        } else {
            return defaultJob;
        }
    }
}
