package brainwine.gameserver.zone.gen.models;

// Well, this is rather embarrassing...
public enum BaseResourceType {
    
    CLAY(new ModTileBlock("ground/clay-1", 1, 1),new ModTileBlock("ground/clay-2", 1, 1), new ModTileBlock("ground/clay-3", 1, 1), 
            new ModTileBlock("ground/clay-4", 1, 1), new ModTileBlock("ground/clay-5", 1, 1), new ModTileBlock("ground/clay-6", 1, 2), 
            new ModTileBlock("ground/clay-7", 1, 2), new ModTileBlock("ground/clay-8", 2, 1), new ModTileBlock("ground/clay-9", 2, 1)),
    
    LOGS(new ModTileBlock("ground/petrified-wood-1", 2, 1), new ModTileBlock("ground/petrified-wood-2", 2, 1),
            new ModTileBlock("ground/petrified-wood-3", 2, 1), new ModTileBlock("ground/petrified-wood-4", 2, 1), 
            new ModTileBlock("ground/petrified-wood-large-1", 4, 2)),
    
    ROOTS(new ModTileBlock("ground/earth-root-wide-3", 2, 1), new ModTileBlock("ground/earth-root-wide-4", 2, 1),
            new ModTileBlock("ground/earth-root-tall-1", 1, 2), new ModTileBlock("ground/earth-root-big-1", 2, 2), 
            new ModTileBlock("ground/earth-root-huge-1", 3, 3)),
    
    ROCKS(new ModTileBlock("ground/earth-rock", 1, 1), new ModTileBlock("ground/earth-rock-wide-1", 2, 1), 
            new ModTileBlock("ground/earth-rock-big-1", 2, 2));
    
    private final ModTileBlock[] blocks;
    
    private BaseResourceType(ModTileBlock... blocks) {
        this.blocks = blocks;
    }
    
    public ModTileBlock[] getBlocks() {
        return blocks;
    }
}
