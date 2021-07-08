package brainwine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.Api;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.TickLoop;

public class Bootstrap {

    private static Logger logger = LogManager.getLogger();
    private final GameServer gameServer;
    private final Api api;
    private boolean shutdown;
    
    public static void main(String[] args) {
        new Bootstrap();
    }
    
    public Bootstrap() {
        logger.warn("NOTE: THIS SERVER IS INCOMPLETE! EXPECT BAD CODE, BUGS, AND MISSING FEATURES!");
        logger.warn("The server will start in 5 seconds");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
        
        logger.info("Bootstrapping ...");
        gameServer = new GameServer();
        api = new Api(new DirectDataFetcher(gameServer.getPlayerManager(), gameServer.getZoneManager()));
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
        logger.info("Bootstrap complete. Type 'stop' in the console to shutdown the server.");
        runTickLoop();
    }
    
    private void runTickLoop() {
        TickLoop loop = new TickLoop(20, () -> {
           gameServer.tick(); 
        });
        
        while(!gameServer.shouldShutdown()) {
            loop.update();
        }
        
        onShutdown();
    }
    
    public void shutdown() {
        gameServer.shutdown();
    }
    
    protected void onShutdown() {
        if(shutdown) {
            return;
        }
        
        shutdown = true;
        logger.info("Shutting down ...");
        
        try {
            gameServer.onShutdown();
            api.onShutdown();
        } catch(Exception e) {
            logger.error("Exception occured whilst shutting down", e);
        }
    }
}
