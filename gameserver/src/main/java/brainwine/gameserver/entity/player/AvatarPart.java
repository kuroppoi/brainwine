package brainwine.gameserver.entity.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AvatarPart {
    
    @JsonProperty("c*")
    SKIN_COLOR,
    
    @JsonProperty("h*")
    HAIR_COLOR,
    
    @JsonProperty("h")
    HAIR,
    
    @JsonProperty("fh")
    FACIAL_HAIR,
    
    @JsonProperty("t")
    TOPS,
    
    @JsonProperty("b")
    BOTTOMS,
    
    @JsonProperty("fw")
    FOOTWEAR,
    
    @JsonProperty("hg")
    HEADGEAR,
    
    @JsonProperty("fg")
    FACIAL_GEAR,
    
    @JsonProperty("fg*")
    FACIAL_GEAR_FLOW,
    
    @JsonProperty("u")
    SUIT,
    
    @JsonProperty("to")
    TOPS_OVERLAY,
    
    @JsonProperty("to*")
    TOPS_OVERLAY_GLOW,
    
    @JsonProperty("ao")
    ARMS_OVERLAY,
    
    @JsonProperty("lo")
    LEGS_OVERLAY,
    
    @JsonProperty("fo")
    FOOTWEAR_OVERLAY;
}
