package brainwine.gameserver.server;

public class MessageOptions {
    
    private final boolean json;
    private final boolean compressed;
    private final boolean collection;
    private final boolean prepacked;
    
    public MessageOptions(RegisterMessage info) {
        this(info.json(), info.compressed(), info.collection(), info.prepacked());
    }
    
    public MessageOptions(boolean json, boolean compressed, boolean collection, boolean prepacked) {
        this.json = json;
        this.compressed = compressed;
        this.collection = collection;
        this.prepacked = prepacked;
    }
    
    public boolean isJson() {
        return json;
    }
    
    public boolean isCompressed() {
        return compressed;
    }
    
    public boolean isCollection() {
        return collection;
    }
    
    public boolean isPrepacked() {
        return prepacked;
    }
}
