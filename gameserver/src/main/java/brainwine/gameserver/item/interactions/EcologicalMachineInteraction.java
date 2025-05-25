package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public abstract class EcologicalMachineInteraction implements ItemInteraction {
    
    protected final EcologicalMachine machine;
    
    public EcologicalMachineInteraction(EcologicalMachine machine) {
        this.machine = machine;
    }
    
    public abstract void interact(Zone zone, Player player, int x, int y);
    
    @Override
    public final void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        Player player = (Player)entity;
        
        // Do nothing if the machine cannot be used yet.
        if(!canUseMachine(zone, player, x, y, metaBlock)) {
            return;
        }
        
        // Handle interaction
        interact(zone, player, x, y);
    }
        
    private boolean canUseMachine(Zone zone, Player player, int x, int y, MetaBlock metaBlock) {
        // Check if machine is already active
        if(metaBlock.getBooleanProperty("activated")) {
            return true;
        }
        
        int totalParts = machine.getPartCount();
        int foundParts = zone.getDiscoveredParts(machine).size();
        
        // Check if parts have been discovered
        if(foundParts < totalParts) {
            int remainingParts = totalParts - foundParts;
            player.notify(String.format("%s part%s of the %s still need%s to be found.",
                    remainingParts, remainingParts == 1 ? "" : "s", machine.getId(), remainingParts == 1 ? "s" : ""));
            return false;
        }
        
        // Activate the machine!
        metaBlock.setProperty("activated", true);
        zone.updateBlock(x, y, Layer.FRONT, machine.getBase(), 2, null, metaBlock.getMetadata()); // TODO
        player.notify(String.format("You activated the %s!", machine.getId()), NotificationType.ACCOMPLISHMENT);
        player.notifyPeers(String.format("%s activated the %s!", player.getName(), machine.getId()), NotificationType.PEER_ACCOMPLISHMENT);
        return false;
    }
}
