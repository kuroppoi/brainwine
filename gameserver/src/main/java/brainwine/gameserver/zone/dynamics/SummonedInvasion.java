package brainwine.gameserver.zone.dynamics;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Zone;

import java.util.List;

public class SummonedInvasion extends Invasion {
    List<Entity> targets;

    private static WeightedMap<String> getInvaderTable(int difficulty) {
        if(difficulty > 3) {
            return new WeightedMap<>(MapHelper.map(
                    String.class, Double.class,
                    "revenant-lord", 1.0
            ));
        } else {
            return new WeightedMap<>(MapHelper.map(
                    String.class, Double.class,
                    "revenant", 1.0
            ));
        }
    }

    public SummonedInvasion(Zone zone, List<Entity> targets, int difficulty, int totalWaves) {
        super(zone, getInvaderTable(difficulty), totalWaves);
        this.targets = targets;
    }

    @Override
    public List<Entity> getTargets() {
        return targets;
    }

    @Override
    public int maxInstances() {
        return 0;
    }

    @Override
    public boolean isFinished() {
        return startTime + 300_000 < System.currentTimeMillis() || super.isFinished();
    }
}
