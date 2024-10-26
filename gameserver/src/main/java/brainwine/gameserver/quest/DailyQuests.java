package brainwine.gameserver.quest;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.ValueWithExpiry;

public class DailyQuests {
    public static void tryIssueDailyQuest(Player player) {
        ValueWithExpiry<Quest> current = player.getDailyQuest();

        if(current == null || current.isExpired()) {
            Quest newQuest = RandomQuests.generateRandomPlayerQuest(player);
            newQuest.setTitle("Daily Quest");

            if(newQuest == null) return;

            if(current.getValue() != null) {
                String oldQuestId = current.getValue().getId();
                PlayerQuests.cancelQuest(player, oldQuestId);

                player.getQuestProgresses().remove(oldQuestId);
                PlayerQuests.sendPlayerCancelQuestMessage(player, oldQuestId);
            }

            player.setDailyQuest(new ValueWithExpiry<>(newQuest, "24h"));
            PlayerQuests.beginQuest(player, newQuest);
            player.notify("You have a new daily quest! Check your quests menu in the top left to see what you need to do!");
        }
    }
}
