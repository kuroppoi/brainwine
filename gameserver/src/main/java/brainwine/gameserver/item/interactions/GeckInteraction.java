package brainwine.gameserver.item.interactions;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

/**
 * Interaction handler for the purifier
 */
public class GeckInteraction extends EcologicalMachineInteraction {

    public GeckInteraction() {
        super(EcologicalMachine.PURIFIER);
    }
    
    @Override
    public void interact(Zone zone, Player player, int x, int y) {
        player.notify("The purifier is working.");
    }
}
