package brainwine.gameserver.item.interactions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.MetaBlock;
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
        // TODO create a more generic function for this
        List<Entity> ghosts = zone.getNpcs().stream()
                .filter(npc -> npc.getConfig().getName().equals("ghost") && npc.inRange(x, y, 5.0))
                .collect(Collectors.toList());
        
        // Check if there are ghosts nearby
        if(ghosts.isEmpty()) {
            player.notify("No ghosts in range.");
            return;
        }
        
        List<MetaBlock> protectors = zone.getMetaBlocksWithItem("hell/dish");
        Collections.shuffle(protectors);
        
        // Expiate nearby ghosts
        for(Entity ghost : ghosts) {
            ghost.setHealth(0.0F);
            
            // Destroy a random infernal protector
            if(!protectors.isEmpty()) {
                MetaBlock protector = protectors.remove(0);
                zone.updateBlock(protector.getX(), protector.getY(), Layer.FRONT, 0);
            }
        }
        
        zone.spawnEffect(x + 2.0F, y, "expiate", 10);
        player.notify("You released a lost soul!", NotificationType.ACCOMPLISHMENT);
        player.notifyPeers(String.format("%s released a lost soul.", player.getName()), NotificationType.PEER_ACCOMPLISHMENT);
        player.getStatistics().trackDeliverances(ghosts.size());
    }
}
