package brainwine.gameserver.util;

public abstract class LazyGetter<I, O> {
    
    protected final I in;
    private O out;
    
    public LazyGetter(I in) {
        this.in = in;
    }
    
    public abstract O load();
    
    public O get() {
        if(out == null) {
            out = load();
        }
        
        return out;
    }
}
