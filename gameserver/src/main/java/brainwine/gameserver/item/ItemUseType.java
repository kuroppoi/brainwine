package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.item.interactions.BurstInteraction;
import brainwine.gameserver.item.interactions.ChangeInteraction;
import brainwine.gameserver.item.interactions.ComposterInteraction;
import brainwine.gameserver.item.interactions.ContainerInteraction;
import brainwine.gameserver.item.interactions.DialogInteraction;
import brainwine.gameserver.item.interactions.ExpiatorInteraction;
import brainwine.gameserver.item.interactions.GeckInteraction;
import brainwine.gameserver.item.interactions.ItemInteraction;
import brainwine.gameserver.item.interactions.NoteInteraction;
import brainwine.gameserver.item.interactions.RecyclerInteraction;
import brainwine.gameserver.item.interactions.SpawnInteraction;
import brainwine.gameserver.item.interactions.SpawnTeleportInteraction;
import brainwine.gameserver.item.interactions.SwitchInteraction;
import brainwine.gameserver.item.interactions.TargetTeleportInteraction;
import brainwine.gameserver.item.interactions.TeleportInteraction;
import brainwine.gameserver.item.interactions.TransmitInteraction;

/**
 * Much like with {@link Action}, block interactions depend on their use type.
 */
public enum ItemUseType {
    
    AFTERBURNER,
    BURST(new BurstInteraction()),
    COMPOSTER(new ComposterInteraction()),
    CONTAINER(new ContainerInteraction()),
    CREATE_DIALOG(new DialogInteraction(true)),
    DESTROY,
    DIALOG(new DialogInteraction(false)),
    EXPIATOR(new ExpiatorInteraction()),
    GECK(new GeckInteraction()),
    GUARD,
    CHANGE(new ChangeInteraction()),
    FIELDABLE,
    FLY,
    MOVE,
    MULTI,
    NOTE(new NoteInteraction()),
    PET,
    PLENTY,
    PROTECTED,
    PUBLIC,
    RECYCLER(new RecyclerInteraction()),
    SPAWN(new SpawnInteraction()),
    SPAWN_TELEPORT(new SpawnTeleportInteraction()),
    SWITCH(new SwitchInteraction()),
    SWITCHED,
    TARGET_TELEPORT(new TargetTeleportInteraction()),
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
