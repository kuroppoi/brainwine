package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.item.consumables.Consumable;
import brainwine.gameserver.item.consumables.ConvertConsumable;
import brainwine.gameserver.item.consumables.HealConsumable;
import brainwine.gameserver.item.consumables.RefillConsumable;
import brainwine.gameserver.item.consumables.TeleportConsumable;

/**
 * Action types for items.
 * 
 * All consumables depend on their action type, but not all items with actions are consumables.
 * This creates a bit of an awkward situation in terms of implementation, but we're just gonna have to deal with that.
 */
public enum Action {
    
	CONVERT(new ConvertConsumable()),
    DIG,
    HEAL(new HealConsumable()),
    REFILL(new RefillConsumable()),
    TELEPORT(new TeleportConsumable()),
    
    @JsonEnumDefaultValue
    NONE;
    
    private final Consumable consumable;
    
    private Action(Consumable consumable) {
    	this.consumable = consumable;
    }
    
    private Action() {
    	this(null);
    }
    
    public Consumable getConsumable() {
    	return consumable;
    }
}
