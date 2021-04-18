package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;

public class LightMessage extends Message {
    
    public int x;
    public int y; // Not used
    public int type; // Not used
    public int[] light;
    
    public LightMessage(int x, int[] light) {
        this.x = x;
        this.light = light;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
