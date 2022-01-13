package brainwine.gameserver.server;

/**
 * Messages are outgoing packets to the client.
 */
public abstract class Message {
    
    public boolean isJson() {
        return false;
    }
    
    public boolean isCompressed() {
        return false;
    }
    
    public boolean isCollection() {
        return false;
    }
    
    public boolean isPrepacked() {
        return false;
    }
}
