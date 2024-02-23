package brainwine.gameserver.entity.player;

public enum AppearanceSlot {

    SKIN_COLOR("c*", "skin-color", true),
    HAIR_COLOR("h*", "hair-color", true),
    HAIR("h", "hair", true),
    FACIAL_HAIR("fh", "facialhair", true),
    TOPS("t", "tops", true),
    BOTTOMS("b", "bottoms", true),
    FOOTWEAR("fw", "footwear", true),
    HEADGEAR("hg", "headgear", true),
    FACIAL_GEAR("fg", "facialgear", true),
    FACIAL_GEAR_GLOW("fg*", "facialgear-glow"),
    SUIT("u", "suit"),
    TOPS_OVERLAY("to", "tops-overlay"),
    TOPS_OVERLAY_GLOW("to*", "tops-overlay-glow"),
    ARMS_OVERLAY("ao", "arms-overlay"),
    LEGS_OVERLAY("lo", "legs-overlay"),
    FOOTWEAR_OVERLAY("fo", "footwear-overlay");
    
    private final String id;
    private final String category;
    private final boolean changeable;

    private AppearanceSlot(String id, String category) {
        this(id, category, false);
    }
    
    private AppearanceSlot(String id, String category, boolean changeable) {
        this.id = id;
        this.category = category;
        this.changeable = changeable;
    }
    
    public static AppearanceSlot fromId(String id) {
        for(AppearanceSlot value : values()) {
            if(value.getId().equals(id)) {
                return value;
            }
        }
        
        return null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getCategory() {
        return category;
    }
    
    public boolean isChangeable() {
        return changeable;
    }
    
    public boolean isColor() {
        return id.endsWith("*");
    }
}
