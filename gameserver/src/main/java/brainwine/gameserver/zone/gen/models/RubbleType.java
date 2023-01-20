package brainwine.gameserver.zone.gen.models;

public enum RubbleType {
    
    WOOD(950, 951, 954, 955, 956, 957, 958, 959),
    STONE(940, 941, 942, 943, 944, 945, 946, 947),
    METAL(980, 981, 982, 983, 984, 985, 986, 987, 988, 989);
    
    private final int[] itemIds;
    
    private RubbleType(int... itemIds) {
        this.itemIds = itemIds;
    }
    
    public int[] getItemIds() {
        return itemIds;
    }
}
