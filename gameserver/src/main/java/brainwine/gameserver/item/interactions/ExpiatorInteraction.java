package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for the expiator
 */
public class ExpiatorInteraction extends EcologicalMachineInteraction {

    public ExpiatorInteraction() {
        super(EcologicalMachine.EXPIATOR);
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y) {
        player.notify("Sorry, not implemented yet ;(");        
    }
}
