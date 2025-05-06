package brainwine.gameserver.zone.dynamics;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Zone;

import java.util.ArrayList;
import java.util.List;

public class SummonedInvasion extends Invasion {
    private int x;
    private int y;

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

    public SummonedInvasion(Zone zone, int x, int y, int difficulty, int totalWaves) {
        super(zone, getInvaderTable(difficulty), totalWaves);
        this.x = x;
        this.y = y;
    }

    @Override
    public List<Entity> getTargets() {
        return new ArrayList<>(zone.getPlayersInRange(x, y, 30.0));
    }

    @Override
    public int maxInstances() {
        return 5;
    }

    @Override
    public boolean isFinished() {
        return startTime + 300_000 < System.currentTimeMillis() || super.isFinished();
    }
}
