package brainwine.gameserver.zone;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ZoneActivity {
    @JsonEnumDefaultValue
    NONE,
    MARKET,
    TUTORIAL,
}
