package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.item.interactions.BurstInteraction;
import brainwine.gameserver.item.interactions.ChangeInteraction;
import brainwine.gameserver.item.interactions.ContainerInteraction;
import brainwine.gameserver.item.interactions.DialogInteraction;
import brainwine.gameserver.item.interactions.ItemInteraction;
import brainwine.gameserver.item.interactions.SpawnInteraction;
import brainwine.gameserver.item.interactions.SpawnTeleportInteraction;
import brainwine.gameserver.item.interactions.SwitchInteraction;
import brainwine.gameserver.item.interactions.TeleportInteraction;
import brainwine.gameserver.item.interactions.TransmitInteraction;

/**
 * Much like with {@link Action}, block interactions depend on their use type.
 */
public enum ItemUseType {
    
    AFTERBURNER,
    BURST(new BurstInteraction()),
    CONTAINER(new ContainerInteraction()),
    CREATE_DIALOG(new DialogInteraction(true)),
    DESTROY,
    DIALOG(new DialogInteraction(false)),
    GUARD,
    CHANGE(new ChangeInteraction()),
    FIELDABLE,
    FLY,
    MULTI,
    PLENTY,
    PROTECTED,
    PUBLIC,
    SPAWN(new SpawnInteraction()),
    SPAWN_TELEPORT(new SpawnTeleportInteraction()),
    SWITCH(new SwitchInteraction()),
    SWITCHED,
    TELEPORT(new TeleportInteraction()),
    TRIGGER,
    TRANSMIT(new TransmitInteraction()),
    TRANSMITTED,
    ZONE_TELEPORT,
    
    @JsonEnumDefaultValue
    UNKNOWN;
    
    private final ItemInteraction interaction;
    
    private ItemUseType(ItemInteraction interaction) {
        this.interaction = interaction;
    }
    
    private ItemUseType() {
        this(null);
    }
    
    @JsonCreator
    public static ItemUseType fromId(String id) {
        String formatted = id.toUpperCase().replace(" ", "_");
        
        for(ItemUseType value : values()) {
            if(value.toString().equals(formatted)) {
                return value;
            }
        }
        
        return UNKNOWN;
    }
    
    public ItemInteraction getInteraction() {
        return interaction;
    }
}
