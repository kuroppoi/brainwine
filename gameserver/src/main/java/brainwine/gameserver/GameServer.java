package brainwine.gameserver;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.PlayerManager;
import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.server.NetworkRegistry;
import brainwine.gameserver.server.Server;
import brainwine.gameserver.zone.ZoneManager;

public class GameServer implements CommandExecutor {
    
    public static final int GLOBAL_SAVE_INTERVAL = 30000; // 30 seconds
    private static final Logger logger = LogManager.getLogger();
    private static GameServer instance;
    private final Thread handlerThread;
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final ZoneManager zoneManager;
    private final PlayerManager playerManager;
    private final Server server;
    private long lastSave = System.currentTimeMillis();
    private boolean shutdownRequested;
    
    public GameServer() {
        instance = this;
        handlerThread = Thread.currentThread();
        long startTime = System.currentTimeMillis();
        logger.info("Starting GameServer ...");
        CommandManager.init();
        GameConfiguration.init();
        MessagePackHelper.init();
        zoneManager = new ZoneManager();
        playerManager = new PlayerManager();
        NetworkRegistry.init();
        server = new Server();
        server.addEndpoint(5002);
        ConsoleThread consoleThread = new ConsoleThread(this);
        consoleThread.setDaemon(true); // otherwise the JVM won't exit.
        consoleThread.start();
        logger.info("All done! GameServer startup took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    public static GameServer getInstance() {
        return instance;
    }
    
    @Override
    public void alert(String text) {
        logger.info(text);
    }
    
    public void tick() {
        synchronized(tasks) {
            while(!tasks.isEmpty()) {
                tasks.poll().run();
            }
        }
        
        if(lastSave + GLOBAL_SAVE_INTERVAL < System.currentTimeMillis()) {
            zoneManager.saveZones();
            playerManager.savePlayers();
            lastSave = System.currentTimeMillis();
        }
        
        zoneManager.tick();
        playerManager.tick();
    }
    
    /**
     * Queues a task to be executed on the handler thread during the next tick.
     * If this function is called from the handler thread, the task is executed immediately.
     * 
     * @param task The task to execute.
     */
    public void queueSynchronousTask(Runnable task) {
        if(Thread.currentThread() == handlerThread) {
            task.run();
        }
        else {
            synchronized(tasks) {
                tasks.add(task);
            }
        }
    }
    
    /**
     * Called by the bootstrapper when the program closes.
     */
    public void onShutdown() {
        logger.info("Shutting down GameServer ...");
        server.close();
        zoneManager.saveZones();
        playerManager.savePlayers();
    }
    
    public void shutdown() {
        shutdownRequested = true;
    }
    
    public boolean shouldShutdown() {
        return shutdownRequested;
    }
    
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
