package brainwine.bootstrap;

public class ShutdownThread extends Thread {
    
    private final Bootstrap bootstrap;
    
    public ShutdownThread(Bootstrap bootstrap) {
        super("shutdown");
        this.bootstrap = bootstrap;
    }
    
    @Override
    public void run() {
        bootstrap.onShutdown();
    }
}
