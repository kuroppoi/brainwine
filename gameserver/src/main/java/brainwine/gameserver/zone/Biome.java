package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.msgpack.EnumValue;
import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum Biome {
    
    @JsonEnumDefaultValue
    PLAIN,
    ARCTIC,
    HELL,
    DESERT,
    BRAIN,
    DEEP,
    SPACE;
    
    @JsonValue
    @EnumValue
    public String getId() {
        return toString().toLowerCase();
    }
    
    @JsonCreator
    public static Biome fromName(String id) {
        for(Biome value : values()) {
            if(value.toString().equalsIgnoreCase(id)) {
                return value;
            }
        }
        
        return PLAIN;
    }
    
    public static Biome getRandomBiome() {
        Biome[] biomes = values();
        return biomes[(int)(Math.random() * biomes.length)];
    }
}
