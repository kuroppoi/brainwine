package brainwine.gameserver.zone;

// Order of the items is important. The dungeon indexer will take the largest ordinal enum value determined.
public enum DungeonType {
    PUZZLE(
            "You raided a dungeon!",
            "%s raided a dungeon.",
            "This container is secured by protectors in the area.",
            100
    ),
    EVOKER(
            "You inhibited an evoker!",
            "%s inhibited an evoker.",
            "This container is secured by evokers in the area.",
            500
    ),
    ;
    private final String selfRaidMessage;
    private final String peerRaidMessage;
    private final String containerProtectedMessage;
    private final int xpReward;

    DungeonType(String selfRaidMessage, String peerRaidMessage, String containerProtectedMessage, int xpReward) {
        this.selfRaidMessage = selfRaidMessage;
        this.peerRaidMessage = peerRaidMessage;
        this.containerProtectedMessage = containerProtectedMessage;
        this.xpReward = xpReward;
    }

    public String getSelfRaidMessage() {
        return selfRaidMessage;
    }

    public String getPeerRaidMessage() {
        return peerRaidMessage;
    }

    public String getContainerProtectedMessage() {
        return containerProtectedMessage;
    }

    public int getXpReward() {
        return xpReward;
    }

    public static DungeonType morePrior(DungeonType a, DungeonType b) {
        if(a == null) a = DungeonType.PUZZLE;
        if(b == null) b = DungeonType.PUZZLE;
        return b.ordinal() > a.ordinal() ? b : a;
    }

    public static DungeonType fromMetaBlock(MetaBlock metaBlock) {
        DungeonType dungeonType;
        switch(metaBlock.getItem().getId()) {
            case "mechanical/spawner-brain":
                dungeonType = DungeonType.EVOKER;
                break;
            default:
                dungeonType = DungeonType.PUZZLE;
        }

        return dungeonType;
    }
}
