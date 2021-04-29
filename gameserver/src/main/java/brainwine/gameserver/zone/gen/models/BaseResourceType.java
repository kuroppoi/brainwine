package brainwine.gameserver.zone.gen.models;

/**
 * Couldn't figure out what else to name it tbh.
 */
public enum BaseResourceType {
    
    CLAY(new ModTileBlock(541, 1, 1), new ModTileBlock(542, 1, 1), new ModTileBlock(543, 1, 1), new ModTileBlock(544, 1, 1), new ModTileBlock(545, 1, 1), new ModTileBlock(546, 1, 2), new ModTileBlock(547, 1, 2), new ModTileBlock(548, 2, 1), new ModTileBlock(549, 2, 1)),
    LOGS(new ModTileBlock(522, 2, 1), new ModTileBlock(523, 2, 1), new ModTileBlock(524, 2, 1), new ModTileBlock(525, 2, 1), new ModTileBlock(526, 4, 2)),
    ROOTS(new ModTileBlock(534, 2, 1), new ModTileBlock(535, 2, 1), new ModTileBlock(536, 1, 2), new ModTileBlock(537, 2, 2), new ModTileBlock(538, 3, 3)),
    ROCKS(new ModTileBlock(528, 1, 1), new ModTileBlock(529, 2, 1), new ModTileBlock(530, 2, 2));
    
    private final ModTileBlock[] blocks;
    
    private BaseResourceType(ModTileBlock... blocks) {
        this.blocks = blocks;
    }
    
    public ModTileBlock[] getBlocks() {
        return blocks;
    }
}
