package brainwine.gameserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TickLoop {
    
    private static final Logger logger = LogManager.getLogger();
    private final long rate;
    private final Runnable tick;
    private long lastTick = System.currentTimeMillis();
    private long msBehind = 0;
    
    public TickLoop(long limit, Runnable tick) {
        rate = 1000 / limit;
        this.tick = tick;
    }
    
    public void update() {
        long now = System.currentTimeMillis();
        long timeSinceLastTick = now - lastTick;
        msBehind += timeSinceLastTick;
        lastTick = now;
        
        while(msBehind > rate) {
            tick.run();
            msBehind -= rate;
        }
        
        try {
            Thread.sleep(Math.max(1, rate - msBehind));
        } catch(InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
    }
}
