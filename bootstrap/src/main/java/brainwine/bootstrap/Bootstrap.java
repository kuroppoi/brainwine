package brainwine.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.Api;
import brainwine.gameserver.GameServer;

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
        api = new Api();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
        runTickLoop();
    }
    
    private void runTickLoop() {
        while(!gameServer.shouldShutdown()) {
            gameServer.tick();
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
