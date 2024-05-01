package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;

@JsonFormat(shape = Shape.ARRAY)
public class EntityItemUseData {
    
    private final int id;
    private final int type;
    private final Item item;
    private final int status;
    
    public EntityItemUseData(Player player) {
        this(player.getId(), 0, player.getHeldItem(), 0);
    }
    
    public EntityItemUseData(int id, int type, Item item, int status) {
        this.id = id;
        this.type = type;
        this.item = item;
        this.status = status;
    }
    
    public int getId() {
        return id;
    }
    
    public int getType() {
        return type;
    }
    
    public Item getItem() {
        return item;
    }
    
    public int getStatus() {
        return status;
    }
}
