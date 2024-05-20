package brainwine.gameserver.item.interactions;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.player.Inventory;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for the composter
 */
public class ComposterInteraction extends EcologicalMachineInteraction {
    
    public static final int COMPOST_EARTH_COST = 10;
    public static final int COMPOST_GIBLETS_COST = 3;
    
    public ComposterInteraction() {
        super(EcologicalMachine.COMPOSTER);
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y) {
        Inventory inventory = player.getInventory();
        Item earth = ItemRegistry.getItem("ground/earth");
        Item giblets = ItemRegistry.getItem("ground/giblets");
        Item compost = ItemRegistry.getItem("ground/earth-compost");
        
        // Check if player has the required items
        if(!inventory.hasItem(earth, COMPOST_EARTH_COST) || !inventory.hasItem(giblets, COMPOST_GIBLETS_COST)) {
            player.notify(String.format("You need %s earth and %s giblets to generate compost.", COMPOST_EARTH_COST, COMPOST_GIBLETS_COST));
            return;
        }
        
        inventory.removeItem(earth, COMPOST_EARTH_COST, true);
        inventory.removeItem(giblets, COMPOST_GIBLETS_COST, true);
        inventory.addItem(compost, true);
        zone.spawnEffect(x + 2.0F, y, "area steam", 10);
    }
}
