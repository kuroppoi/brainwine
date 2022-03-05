package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.server.Message;

public class DialogMessage extends Message {
    
    public int id;
    public Object dialog;
    
    public DialogMessage(int id, Dialog dialog) {
        this.id = id;
        this.dialog = dialog;
    }
    
    public DialogMessage(int id, Map<String, Object> dialog) {
        this.id = id;
        this.dialog = dialog;
    }
    
    @Override
    public boolean isCompressed() {
        return true;
    }
}
