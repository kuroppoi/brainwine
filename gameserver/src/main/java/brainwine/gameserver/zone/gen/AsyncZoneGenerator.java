package brainwine.gameserver.zone.gen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.TickLoop;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

public class AsyncZoneGenerator extends Thread {
    
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
                Zone zone = StaticZoneGenerator.generateZone(task.getBiome(), task.getWidth(), task.getHeight(), task.getSeed());
                zoneManager.saveZone(zone);
                System.gc();
                AsyncZoneGeneratedHandler callback = task.getCallback();
                
                if(callback != null) {
                    GameServer.getInstance().queueSynchronousTask(new Runnable() {
                        @Override
                        public void run() {
                            callback.handle(zone);
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
