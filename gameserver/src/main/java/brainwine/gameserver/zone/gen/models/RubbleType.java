package brainwine.gameserver.zone.gen.models;

public enum RubbleType {
    
    WOOD("rubble/wood-stake-1", "rubble/wood-stake-2", "rubble/wood-1", "rubble/wood-2", 
            "rubble/wood-3", "rubble/wood-4", "rubble/wood-5", "rubble/wood-6"),
    
    STONE("rubble/stone-pile-1", "rubble/stone-pile-2", "rubble/stone-pile-3", "rubble/stone-pile-4",
            "rubble/stone-pile-5", "rubble/stone-pile-6", "rubble/stone-pile-7", "rubble/stone-pile-8"),
    
    METAL("rubble/scrap-metal-1", "rubble/scrap-metal-2", "rubble/scrap-metal-3", "rubble/scrap-metal-4",
            "rubble/scrap-metal-5", "rubble/scrap-metal-6", "rubble/scrap-metal-7", "rubble/scrap-metal-8",
            "rubble/scrap-metal-9", "rubble/scrap-metal-10");
    
    private final String[] itemIds;
    
    private RubbleType(String... itemIds) {
        this.itemIds = itemIds;
    }
    
    public String[] getItemIds() {
        return itemIds;
    }
}
