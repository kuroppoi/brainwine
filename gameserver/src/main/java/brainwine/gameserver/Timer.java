package brainwine.gameserver;

/**
 * Model for synchronous timers.
 */
public class Timer<T> {
    
    private T key;
    private long time;
    private Runnable action;
    
    public Timer(T key, long delay, Runnable action) {
        this.key = key;
        this.time = System.currentTimeMillis() + delay;
        this.action = action;
    }
    
    public boolean process() {
        if(System.currentTimeMillis() >= time) {
            action.run();
            return true;
        }
        
        return false;
    }
    
    public T getKey() {
        return key;
    }
    
    public long getTime() {
        return time;
    }
    
    public Runnable getAction() {
        return action;
    }
}
