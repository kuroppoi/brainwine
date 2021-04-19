package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.dialog.ConfigurableDialog;
import brainwine.gameserver.server.Message;

public class DialogMessage extends Message {
    
    public int id;
    public Map<String, Object> config;
    
    public DialogMessage(int id, ConfigurableDialog dialog) {
        this(id, dialog.getClientConfig());
    }
    
    public DialogMessage(int id, Map<String, Object> config) {
        this.id = id;
        this.config = config;
    }
    
    @Override
    public boolean isCompressed() {
        return true;
    }
}
