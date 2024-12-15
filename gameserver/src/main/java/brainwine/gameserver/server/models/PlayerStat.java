package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.server.messages.StatMessage;

/**
 * Easy enum for use with {@link StatMessage}.
 */
public enum PlayerStat {
    
    BREATH,
    CROWNS,
    POINTS,
    THIRST;
    
    @JsonValue
    public String getClientId() {
        return toString().toLowerCase();
    }
}
