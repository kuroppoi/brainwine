package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.ValueWithExpiry;

public class DailyQuests {
    public static void tryIssueDailyQuest(Player player) {
        ValueWithExpiry<List<Quest>> currentV = player.getDailyQuest();

        if(currentV == null || currentV.isExpired()) {
            List<Quest> newQuests = new ArrayList<>();
            for(int i = 1; i <= RandomQuests.getConfiguration().dailyQuestCount; i++) {
                Quest newQuest = RandomQuests.generateRandomPlayerQuest(player);
                if(newQuest == null) return;
                newQuest.setId("daily_player_quest_" + i);
                newQuest.setTitle("Daily Quest #" + i);
                newQuests.add(newQuest);
            }

            if(currentV.getValue() != null) {
                for(Quest current : currentV.getValue()) {
                    String oldQuestId = current.getId();
                    PlayerQuests.cancelQuest(player, oldQuestId);

                    player.getQuestProgresses().remove(oldQuestId);
                    PlayerQuests.sendPlayerCancelQuestMessage(player, current);
                }
            }

            player.setDailyQuest(new ValueWithExpiry<>(newQuests, RandomQuests.getConfiguration().dailyQuestInterval));
            for(Quest newQuest : newQuests) {
                PlayerQuests.beginQuest(player, newQuest);
            }
            
            if(newQuests.size() == 1) {
                player.notify("You have a new daily quest! Check your quests menu in the top left to see what you need to do!");
            } else {
                player.notify("You have new daily quests! Check your quests menu in the top left to see what you need to do!");
            }
        }
    }
}
