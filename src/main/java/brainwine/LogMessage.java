package brainwine;

import org.apache.logging.log4j.Level;

public class LogMessage {
    
    private final Level level;
    private final String message;
    private final String formattedMessage;
    
    public LogMessage(Level level, String message, String formattedMessage) {
        this.level = level;
        this.message = message;
        this.formattedMessage = formattedMessage;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getFormattedMessage() {
        return formattedMessage;
    }
}
