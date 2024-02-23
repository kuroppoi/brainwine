package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.models.EntityItemUseData;

@MessageInfo(id = 10, prepacked = true)
public class EntityItemUseMessage extends Message {
    
    public Collection<EntityItemUseData> data;
    
    public EntityItemUseMessage(Collection<? extends Player> players) {
        this.data = players.stream().map(EntityItemUseData::new).collect(Collectors.toList());
    }
    
    public EntityItemUseMessage(Player player) {
        this.data = Arrays.asList(new EntityItemUseData(player));
    }
    
    public EntityItemUseMessage(int id, int type, Item item, int status) {
        this.data = Arrays.asList(new EntityItemUseData(id, type, item, status));
    }
}
