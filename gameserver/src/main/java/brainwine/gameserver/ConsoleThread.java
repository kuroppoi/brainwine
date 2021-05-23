package brainwine.gameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.NotificationType;

public class ConsoleThread extends Thread implements CommandExecutor {
    
    private static final Logger logger = LogManager.getLogger("Console");
    private final GameServer server;
    
    public ConsoleThread(GameServer server) {
        super("console");
        this.server = server;
    }
    
    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        
        try {
            while((line = reader.readLine()) != null) {
                queueConsoleCommand(line);
            }
        } catch(IOException e) {
            logger.error("Could not read console input", e);
        }
    }
    
    private void queueConsoleCommand(String commandLine) {
        server.queueSynchronousTask(new Runnable() {
            @Override
            public void run() {
                CommandManager.executeCommand(ConsoleThread.this, commandLine);
            }
        });
    }

    @Override
    public void notify(Object message, NotificationType type) {
        logger.info(message);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
