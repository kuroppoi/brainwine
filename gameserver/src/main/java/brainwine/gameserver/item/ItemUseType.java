package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.item.interactions.ChangeInteraction;
import brainwine.gameserver.item.interactions.ContainerInteraction;
import brainwine.gameserver.item.interactions.DialogInteraction;
import brainwine.gameserver.item.interactions.ItemInteraction;
import brainwine.gameserver.item.interactions.SwitchInteraction;
import brainwine.gameserver.item.interactions.TeleportInteraction;

/**
 * Much like with {@link Action}, block interactions depend on their use type.
 */
public enum ItemUseType {
    
    AFTERBURNER,
    CONTAINER(new ContainerInteraction()),
    CREATE_DIALOG(new DialogInteraction(true)),
    DIALOG(new DialogInteraction(false)),
    GUARD,
    CHANGE(new ChangeInteraction()),
    FIELDABLE,
    FLY,
    MULTI,
    PLENTY,
    PROTECTED,
    PUBLIC,
    SWITCH(new SwitchInteraction()),
    SWITCHED,
    TELEPORT(new TeleportInteraction()),
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
