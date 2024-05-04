package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for the composter
 */
public class ComposterInteraction extends EcologicalMachineInteraction {

    public ComposterInteraction() {
        super(EcologicalMachine.COMPOSTER);
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y) {
        player.notify("Sorry, not implemented yet ;(");        
    }
}
