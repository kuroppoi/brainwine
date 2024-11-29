package brainwine.gameserver.server.messages;

import java.util.Map;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

        
@MessageInfo(id = 63, collection = true)
public class QuestMessage extends Message {
    public Map<String, Object> details;
    public Map<String, Object> status;

    public QuestMessage(Map<String, Object> clientDetails, Map<String, Object> clientStatus) {
        details = clientDetails;
        status = clientStatus;
    }
}
