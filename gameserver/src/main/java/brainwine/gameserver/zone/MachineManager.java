package brainwine.gameserver.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.util.MapHelper;

/**
 * Manages ecological machines in a zone.
 */
public class MachineManager {
    
    public static final float PURIFICATION_TIME_SECONDS = 60.0F * 60.0F * 24.0F * 3.0F; // 72 hours (3 days)
    private final Map<EcologicalMachine, List<Item>> discoveredParts = new HashMap<>();
    private final Map<Integer, MetaBlock> machineBlocks = new HashMap<>();
    private final Zone zone;
    
    public MachineManager(Zone zone) {
        this.zone = zone;
    }
    
    /**
     * Decreases the zone's acidity if the purifier has been activated.
     */
    public void updatePurifier(float deltaTime) {
        // Do nothing if purifier isn't active
        if(!isMachineActive(EcologicalMachine.PURIFIER)) {
            return;
        }
        
        // Reduce acidity
        float amount = deltaTime / PURIFICATION_TIME_SECONDS;
        zone.setAcidity(Math.max(0.0F, zone.getAcidity() - amount));
    }
    
    /**
     * @return {@code true} if the specified machine has been activated, otherwise {@code false}.
     */
    public boolean isMachineActive(EcologicalMachine machine) {
        return machineBlocks.values().stream()
                .filter(block -> block.getItem() == machine.getBase() && block.getBooleanProperty("activated"))
                .findFirst().isPresent();
    }
    
    /**
     * This will show the number of discovered components on the minimap
     * as well as update the machine sprite on V2 clients.
     */
    public void sendMachineStatus(Player player) {
        // V3 unfortunately doesn't seem to track machine progress
        if(player.isV3()) {
            return;
        }
        
        // Create client data
        Map<String, Object> data = discoveredParts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getClientId(), // Map machine instance to client ID
                        entry -> entry.getValue().stream()
                            .map(Item::getCode) // Map item instance to item code
                            .collect(Collectors.toCollection(ArrayList::new))));
        
        // Sneakily add base parts of active machines to client data
        List<EcologicalMachine> activeMachines = Stream.of(EcologicalMachine.values())
                .filter(this::isMachineActive)
                .collect(Collectors.toList());
        activeMachines.forEach(machine -> MapHelper.appendList(data, machine.getClientId(), machine.getBase().getCode()));
        
        // Send data
        player.sendMessage(new ZoneStatusMessage(MapHelper.map("machines", data)));
    }
    
    /**
     * Sync machine status with all players in the zone.
     */
    private void updateMachineStatus(EcologicalMachine machine) {
        // Find all machines of this type in the zone
        List<MetaBlock> metaBlocks = machineBlocks.values().stream()
                .filter(block -> block.getItem() == machine.getBase())
                .collect(Collectors.toList());
        
        // Get list of discovered parts and transform to client data
        List<Integer> parts = discoveredParts.getOrDefault(machine, Collections.emptyList()).stream()
                .filter(machine::isMachinePart)
                .map(Item::getCode)
                .collect(Collectors.toList());
        parts.add(machine.getBase().getCode());
        
        // Update the machine sprite for V3 clients
        for(MetaBlock metaBlock : metaBlocks) {
            metaBlock.setProperty("spr", parts);
            zone.sendBlockMetaUpdate(metaBlock);
        }
        
        // Send machine status update to players
        for(Player player : zone.getPlayers()) {
            sendMachineStatus(player);
        }
    }
    
    // TODO rethink
    public boolean addMachinePart(Item part) {
        EcologicalMachine machine = EcologicalMachine.fromPart(part);
        
        // Do nothing if machine doesn't exist
        if(machine == null) {
            return false;
        }
        
        List<Item> parts = discoveredParts.computeIfAbsent(machine, x -> new ArrayList<>());
        
        // Do nothing if part has already been discovered
        if(parts.contains(part)) {
            return false;
        }
        
        // Update machine status
        parts.add(part);
        updateMachineStatus(machine);
        return true;
    }
    
    // TODO rethink
    public boolean removeMachinePart(Item part) {
        EcologicalMachine machine = EcologicalMachine.fromPart(part);
        
        // Do nothing if machine doesn't exist
        if(machine == null) {
            return false;
        }
        
        List<Item> parts = discoveredParts.get(machine);
        
        if(parts != null) {
            parts.remove(part);
            
            // Remove key if there are no parts left
            if(parts.isEmpty()) {
                discoveredParts.remove(machine);
            }
            
            // Update machine status
            updateMachineStatus(machine);
            return true;
        }
        
        return false;
    }
    
    /**
     * @return An immutable view of the discovered parts for the specified machine.
     */
    public Collection<Item> getDiscoveredParts(EcologicalMachine machine) {
        return Collections.unmodifiableCollection(discoveredParts.getOrDefault(machine, Collections.emptyList()));
    }
    
    /**
     * @return An immutable view of all of the discovered machine parts.
     */
    public Map<EcologicalMachine, List<Item>> getDiscoveredParts() {
        return Collections.unmodifiableMap(discoveredParts);
    }
    
    protected void loadData(ZoneConfigFile config) {
        // Filter out invalid
        Map<EcologicalMachine, List<Item>> discoveredParts = config.getDiscoveredParts().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().stream()
                            .filter(item -> entry.getKey().isMachinePart(item))
                            .collect(Collectors.toCollection(ArrayList::new))));
        this.discoveredParts.putAll(discoveredParts);
    }
    
    protected void indexMetaBlock(int index, MetaBlock metaBlock) {
        EcologicalMachine machine = EcologicalMachine.fromBase(metaBlock.getItem());
        
        if(machine != null) {
            machineBlocks.put(index, metaBlock);
            updateMachineStatus(machine);
        }
    }
    
    protected void unindexMetaBlock(int index) {
        MetaBlock metaBlock = machineBlocks.remove(index);
        
        if(metaBlock != null) {
            updateMachineStatus(EcologicalMachine.fromBase(metaBlock.getItem()));
        }
    }
}
