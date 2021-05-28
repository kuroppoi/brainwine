package brainwine.gameserver.zone.gen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.TickLoop;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

public class AsyncZoneGenerator extends Thread {
    
    private static final Logger logger = LogManager.getLogger();
    private final Queue<AsyncZoneGeneratorTask> tasks = new ConcurrentLinkedQueue<>();
    private final ZoneManager zoneManager;
    private boolean running;
    
    public AsyncZoneGenerator(ZoneManager zoneManager) {
        super("zone-generator");
        this.zoneManager = zoneManager;
    }
    
    @Override
    public void run() {
        TickLoop loop = new TickLoop(1, () -> {
            while(!tasks.isEmpty()) {
                AsyncZoneGeneratorTask task = tasks.poll();
                Biome biome = task.getBiome();
                int width = task.getWidth();
                int height = task.getHeight();
                int seed = task.getSeed();
                Zone zone = null;
                
                try {
                    zone = StaticZoneGenerator.generateZone(biome, width, height, seed);
                    zoneManager.saveZone(zone);
                } catch(Exception e) {
                    logger.error("An unexpected error occured while generating zone [biome:{}, width:{}, height:{}, seed:{}]", biome, width, height, seed, e);
                }
                
                System.gc();
                Zone generated = zone;
                AsyncZoneGeneratedHandler callback = task.getCallback();
                
                if(callback != null) {
                    GameServer.getInstance().queueSynchronousTask(new Runnable() {
                        @Override
                        public void run() {
                            callback.handle(generated);
                        }
                    });
                }
            }
        });
        
        running = true;
        
        while(running) {
            loop.update();
        }
    }
    
    public void generateZone(Biome biome, int width, int height, int seed, AsyncZoneGeneratedHandler callback) {
        tasks.add(new AsyncZoneGeneratorTask(biome, width, height, seed, callback));
    }
}
