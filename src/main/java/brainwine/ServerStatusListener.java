package brainwine;

public interface ServerStatusListener {
    
    public void onServerStarting();
    public void onServerStopping();
    public void onServerStarted();
    public void onServerStopped();
}
