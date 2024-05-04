package brainwine.gameserver.item.interactions;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public abstract class EcologicalMachineInteraction implements ItemInteraction {
    
    protected final EcologicalMachine machine;
    
    public EcologicalMachineInteraction(EcologicalMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public final void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock,
            Object config, Object[] data) {
        // Do nothing if entity is not a player
        if(!entity.isPlayer()) {
            return;
        }
        
        Player player = (Player)entity;
        
        // Do nothing if the machine cannot be used yet.
        if(!zone.canUseMachine(machine, player, x, y)) {
            return;
        }
        
        // Handle interaction
        interact(zone, player, x, y);
    }
    
    public abstract void interact(Zone zone, Player player, int x, int y);
}
