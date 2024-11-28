package brainwine.gameserver.achievement;

import brainwine.gameserver.player.Player;

public class ArchitectAchievement extends Achievement {
    public ArchitectAchievement(String title) {
        super(title);
        System.out.println("CONSTRUCTED");
    }

    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getLandmarkVotesReceived();
    }
}
