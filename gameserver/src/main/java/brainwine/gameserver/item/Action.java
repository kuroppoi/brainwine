package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.item.consumables.Consumable;
import brainwine.gameserver.item.consumables.ConvertConsumable;

/**
 * Action types for items.
 * 
 * All consumables depend on their action type, but not all items with actions are consumables.
 * This creates a bit of an awkward situation in terms of implementation, but we're just gonna have to deal with that.
 */
public enum Action {
    
	CONVERT(new ConvertConsumable()),
    DIG,
    HEAL,
    REFILL,
    
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
