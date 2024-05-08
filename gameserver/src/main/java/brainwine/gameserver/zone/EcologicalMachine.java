package brainwine.gameserver.zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;

public enum EcologicalMachine {
    
    PURIFIER("p", "mechanical/geck-tub", 
            "mechanical/geck-tank-base", 
            "mechanical/geck-tank", 
            "mechanical/geck-hoses",
            "mechanical/geck-tree-base",
            "mechanical/geck-tree-top",
            "mechanical/geck-cog-large",
            "mechanical/geck-cog-small"),
    
    COMPOSTER("c", "mechanical/composter-chamber",
            "mechanical/composter-cover",
            "mechanical/composter-fuel-tank",
            "mechanical/composter-input-tube",
            "mechanical/composter-output",
            "mechanical/composter-turbine"),
    
    RECYCLER("r", "mechanical/recycler-chamber",
            "mechanical/recycler-tubes",
            "mechanical/recycler-pipe-base",
            "mechanical/recycler-pipe",
            "mechanical/recycler-gear-base",
            "mechanical/recycler-gear"),
    
    // Not exactly 'ecological' but whatever.
    EXPIATOR("e", "hell/expiator-face",
            "hell/expiator-pipe",
            "hell/expiator-frame",
            "hell/expiator-tank",
            "hell/expiator-tubes",
            "hell/expiator-tube",
            "hell/expiator-gear",
            "hell/expiator-exhaust");
    
    private final String clientId;
    private final String base;
    private final List<String> parts;
    
    private EcologicalMachine(String clientId, String base, String... parts) {
        this.clientId = clientId;
        this.base = base;
        this.parts = Arrays.asList(parts);
    }
    
    @JsonCreator
    public static EcologicalMachine fromName(String id) {
        for(EcologicalMachine value : values()) {
            if(value.getId().equalsIgnoreCase(id)) {
                return value;
            }
        }
        
        return null;
    }
    
    public static EcologicalMachine fromBase(Item base) {
        return Stream.of(values()).filter(x -> base.hasId(x.base)).findFirst().orElse(null);
    }
    
    public static EcologicalMachine fromPart(Item part) {
        return Stream.of(values()).filter(x -> x.isMachinePart(part)).findFirst().orElse(null);
    }
    
    public static boolean isMachine(Item item) {
        return fromBase(item) != null;
    }
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public Item getBase() {
        return ItemRegistry.getItem(base);
    }
    
    public boolean isMachinePart(Item item) {
        return parts.contains(item.getId());
    }
    
    public int getPartCount() {
        return parts.size();
    }
    
    public List<Item> getParts() {
        return parts.stream().map(ItemRegistry::getItem).collect(Collectors.toCollection(ArrayList::new));
    }
}
