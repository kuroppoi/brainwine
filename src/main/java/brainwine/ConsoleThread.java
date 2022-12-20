package brainwine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleThread extends Thread {
    
    private static final Logger logger = LogManager.getLogger();
    private final List<Consumer<String>> listeners = new ArrayList<>();
    
    public ConsoleThread() {
        super("console");
        setDaemon(true);
    }
    
    @Override
    public void run() {
        logger.info("Starting console thread ...");
        BufferedReader systemInputReader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        
        try {
            while((line = systemInputReader.readLine()) != null) {
                for(Consumer<String> listener : listeners) {
                    listener.accept(line);
                }
            }
        } catch(IOException e) {
            // What do we do, what do we do!?
            logger.error("An error has occured whilst reading console input", e);
        }        
    }
    
    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }
}
