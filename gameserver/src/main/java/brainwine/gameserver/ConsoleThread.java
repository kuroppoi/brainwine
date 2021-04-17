package brainwine.gameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleThread extends Thread {
    
    private static final Logger logger = LogManager.getLogger();
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
                if(line.equalsIgnoreCase("stop")) {
                    server.shutdown();
                }
            }
        } catch(IOException e) {
            logger.info("Could not read console input", e);
        }
    }
}
