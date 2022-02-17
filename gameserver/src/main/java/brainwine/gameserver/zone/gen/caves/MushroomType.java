package brainwine.gameserver.zone.gen.caves;

public enum MushroomType {
    
    PORTABELLA(746),
    OYSTER(745),
    PORCINI(747),
    WILLOW(735, 736, 3),
    AMANITA(730, 731, 3),
    CHANTERELLE(748),
    MOREL(744, 449),
    ANBARIC(743, 487),
    ACID(741, 742),
    LAVA(750, 447),
    ARCTIC(751, 446),
    HELL(749, 448),
    SHADE(1169),
    APOSTATE(1167, 445),
    DEVILS_CIGAR(1168);
    
    private final int item;
    private int elder;
    private int stalk;
    private int maxHeight;
    
    private MushroomType(int item) {
        this.item = item;
    }
    
    private MushroomType(int item, int stalk, int maxHeight) {
        this.item = item;
        this.stalk = stalk;
        this.maxHeight = maxHeight;
    }
    
    private MushroomType(int item, int elder) {
        this.item = item;
        this.elder = elder;
    }
    
    public int getItem() {
        return item;
    }
    
    public boolean hasElder() {
        return elder != 0;
    }
    
    public int getElder() {
        return elder;
    }
    
    public boolean hasStalk() {
        return stalk != 0;
    }
    
    public int getStalk() {
        return stalk;
    }
    
    public int getMaxHeight() {
        return maxHeight;
    }
}