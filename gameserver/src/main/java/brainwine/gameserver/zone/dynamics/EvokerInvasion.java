package brainwine.gameserver.zone.dynamics;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Zone;

import java.util.Arrays;
import java.util.List;

public class EvokerInvasion extends Invasion {
    private Player target;

    private static WeightedMap<String> getInvaderTable(int difficulty) {
        if(difficulty > 3) {
            return new WeightedMap<>(MapHelper.map(
                    String.class, Double.class,
                    "brains/small", 15.0,
                    "brains/medium", 2.0,
                    "brains/medium-dire", 1.0
            ));
        } else {
            return new WeightedMap<>(MapHelper.map(
                    String.class, Double.class,
                    "brains/small", 15.0
            ));
        }
    }

    public EvokerInvasion(Zone zone, Player target, int difficulty, int totalWaves) {
        super(zone, getInvaderTable(difficulty), totalWaves);
        this.target = target;
    }

    @Override
    public List<Entity> getTargets() {
        return Arrays.asList(target);
    }

    @Override
    public int maxInstances() {
        return 1;
    }
}
