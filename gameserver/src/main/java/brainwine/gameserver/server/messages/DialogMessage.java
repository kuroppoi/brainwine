package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 45, compressed = true)
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
}
