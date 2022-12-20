package brainwine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.Api;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.TickLoop;
import brainwine.gameserver.command.CommandManager;

public class ServerThread extends Thread {
    
    private static Logger logger = LogManager.getLogger();
    private final Bootstrap bootstrap;
    private GameServer gameServer;
    private Api api;
    private boolean running;
    
    public ServerThread(Bootstrap bootstrap) {
        super("server");
        this.bootstrap = bootstrap;
    }
    
    @Override
    public void run() {
        try {
            logger.warn("NOTE: THIS SERVER IS INCOMPLETE! EXPECT BAD CODE, BUGS, AND MISSING FEATURES!");
            logger.info("Starting server ...");
            gameServer = new GameServer();
            api = new Api(new DirectDataFetcher(gameServer.getPlayerManager(), gameServer.getZoneManager()));
            TickLoop tickLoop = new TickLoop(8, () -> {
                gameServer.tick();
            });
            
            logger.info("Server has started");
            running = true;
            bootstrap.onServerStarted();
            
            while(!gameServer.shouldStop()) {
                tickLoop.update();
            }
        } catch(Exception e) {
            logger.error("An unexpected exception occured", e);
        } finally {
            stopUnsafe();
        }
    }
    
    /**
     * Executes a console command on the server thread.
     */
    public void executeCommand(String commandLine) {
        if(gameServer != null) {
            gameServer.queueSynchronousTask(() -> CommandManager.executeCommand(gameServer, commandLine));
        }
    }
    
    public void stopGracefully() {
        if(gameServer != null) {
            gameServer.stopGracefully();
        }
    }
    
    private void stopUnsafe() {
        try {
            logger.info("Stopping server ...");
            gameServer.onShutdown();
            api.onShutdown();
            logger.info("Server has stopped");
        } catch(Exception e) {
            logger.error("An unexpected exception occured whilst shutting down", e);
        } finally {
            running = false;
            bootstrap.onServerStopped();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}
