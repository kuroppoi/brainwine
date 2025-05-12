package brainwine.gameserver.zone.dynamics;

import brainwine.gameserver.zone.Zone;

public class ZoneDynamic {
    protected long startTime;
    protected Zone zone;

    public ZoneDynamic(Zone zone) {
        startTime = System.currentTimeMillis();
        this.zone = zone;
    }

    /**Called every frame.*/
    public void tick(float deltaTime) {}

    /**Start time estimated by calling System.currentTimeMillis()*/
    public long getStartTime() {
        return startTime;
    }

    /**Return true if the invasion has finished so the zone can stop tracking it.*/
    public boolean isFinished() {
        return true;
    }

    /**Return a number greater than 0 if there can be only a given number of ongoing dynamics of type.*/
    public int maxInstances() {
        return 1;
    }

    /**Called immediately after the dynamic is stopped tracking.*/
    public void close() {}

}
