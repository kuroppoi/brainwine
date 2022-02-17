package brainwine.gameserver.zone.gen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.TickLoop;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

public class AsyncZoneGenerator extends Thread {
    
    private static final Logger logger = LogManager.getLogger();
    private final Queue<AsyncZoneGeneratorTask> tasks = new ConcurrentLinkedQueue<>();
    private boolean running;
    
    public AsyncZoneGenerator() {
        super("zonegen");
    }
    
    @Override
    public void run() {
        TickLoop loop = new TickLoop(1, () -> {
            while(!tasks.isEmpty()) {
                AsyncZoneGeneratorTask task = tasks.poll();
                ZoneGenerator generator = task.getGenerator();
                Biome biome = task.getBiome();
                int width = task.getWidth();
                int height = task.getHeight();
                int seed = task.getSeed();
                Zone zone = null;
                
                try {
                    zone = generator.generateZone(biome, width, height, seed);
                } catch(Exception e) {
                    logger.error("An unexpected error occured while generating zone [biome:{}, width:{}, height:{}, seed:{}]", biome, width, height, generator, seed, e);
                }
                
                Zone generated = zone;
                Consumer<Zone> callback = task.getCallback();
                
                if(callback != null) {
                    GameServer.getInstance().queueSynchronousTask(() -> callback.accept(generated));
                }
            }
        });
        
        running = true;
        
        while(running) {
            loop.update();
        }
    }
    
    public void addTask(AsyncZoneGeneratorTask task) {
        tasks.add(task);
    }
    
    public void addTask(ZoneGenerator generator, Biome biome, int width, int height, int seed, Consumer<Zone> callback) {
        addTask(new AsyncZoneGeneratorTask(generator, biome, width, height, seed, callback));
    }
}
