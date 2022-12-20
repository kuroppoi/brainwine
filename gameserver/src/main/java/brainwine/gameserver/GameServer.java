package brainwine.gameserver;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.achievements.AchievementManager;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.PlayerManager;
import brainwine.gameserver.loot.LootManager;
import brainwine.gameserver.prefab.PrefabManager;
import brainwine.gameserver.server.NetworkRegistry;
import brainwine.gameserver.server.Server;
import brainwine.gameserver.zone.EntityManager;
import brainwine.gameserver.zone.ZoneManager;
import brainwine.gameserver.zone.gen.ZoneGenerator;

public class GameServer implements CommandExecutor {
    
    public static final int GLOBAL_SAVE_INTERVAL = 30000; // 30 seconds
    private static final Logger logger = LogManager.getLogger();
    private static final Logger consoleLogger = LogManager.getLogger("Console");
    private static GameServer instance;
    private final Thread handlerThread;
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final LootManager lootManager;
    private final PrefabManager prefabManager;
    private final ZoneManager zoneManager;
    private final PlayerManager playerManager;
    private final Server server;
    private long lastTick = System.currentTimeMillis();
    private long lastSave = lastTick;
    private volatile boolean shouldStop;
    
    public GameServer() {
        instance = this;
        handlerThread = Thread.currentThread();
        long startTime = System.currentTimeMillis();
        logger.info("Starting GameServer ...");
        CommandManager.init();
        GameConfiguration.init();
        AchievementManager.loadAchievements();
        EntityRegistry.init();
        EntityManager.loadEntitySpawns();
        lootManager = new LootManager();
        prefabManager = new PrefabManager();
        ZoneGenerator.init();
        zoneManager = new ZoneManager();
        playerManager = new PlayerManager();
        NetworkRegistry.init();
        server = new Server();
        server.addEndpoint(5002);
        logger.info("All done! GameServer startup took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    public static GameServer getInstance() {
        return instance;
    }
    
    @Override
    public void notify(Object message, NotificationType type) {
        consoleLogger.info(message);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
    
    public void tick() {
        long now = System.currentTimeMillis();
        float deltaTime = (now - lastTick) / 1000.0F; // in seconds
        lastTick = now;
        
        while(!tasks.isEmpty()) {
            tasks.poll().run();
        }
        
        if(lastSave + GLOBAL_SAVE_INTERVAL < System.currentTimeMillis()) {
            zoneManager.saveZones();
            playerManager.savePlayers();
            lastSave = System.currentTimeMillis();
        }
        
        zoneManager.tick(deltaTime);
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
        } else {
            tasks.add(task);
        }
    }
    
    /**
     * Called by the bootstrapper when the program closes.
     */
    public void onShutdown() {
        logger.info("Shutting down GameServer ...");
        server.close();
        ZoneGenerator.stopAsyncZoneGenerator(true);
        logger.info("Saving zone data ...");
        zoneManager.onShutdown();
        logger.info("Saving player data ...");
        playerManager.savePlayers();
    }
    
    public void stopGracefully() {
        shouldStop = true;
    }
    
    public boolean shouldStop() {
        return shouldStop;
    }
    
    public LootManager getLootManager() {
        return lootManager;
    }
    
    public PrefabManager getPrefabManager() {
        return prefabManager;
    }
    
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
