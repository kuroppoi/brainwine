package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.QuestMessage;
import brainwine.gameserver.util.MapHelper;

public class PlayerQuests {
    private PlayerQuests() {}

    public static void beginQuest(Player player, Quest quest) {
        if(player == null) return;
        
        if(quest == null) {
            player.notify("Quest not found!");
            return;
        }

        List<Integer> progresses = new ArrayList<>(quest.getTasks().size());

        for(int i = 0; i < quest.getTasks().size(); i++) {
            progresses.add(0);
        }

        QuestProgress progress = new QuestProgress(quest.getId(), progresses);
        
        player.getQuestProgresses().put(quest.getId(), progress);

        player.notify("Quest has started! Use the /quests command to view your progress at any time.");
        sendPlayerQuestMessage(player, progress);
        performAction(player, quest, QuestAction.Type.BEGIN);
    }

    public static boolean isTaskComplete(Quest quest, Player player, int i) {
        int progress = player.getQuestProgresses().get(quest.getId()).getTaskProgress(i);

        QuestTask task = quest.getTasks().get(i);

        boolean satisfiesQuantity = task.getQuantity() <= progress;
        boolean satisfiesInventory = task.getCollectInventory() == null || task.getCollectInventory().playerSatisfies(player);

        return satisfiesQuantity && satisfiesInventory;
    }

    public static boolean canFinishQuest(Player player, Quest quest) {
        if(player == null) return false;
        
        if(quest == null) {
            player.notify("Quest not found!");
            return false;
        }

        QuestProgress progress = player.getQuestProgresses().get(quest.getId());

        if(progress == null) {
            player.notify("Your quest progress is not found!");
            return false;
        }

        for(int i = 0; i < quest.getTasks().size(); i++) {
            if(!isTaskComplete(quest, player, i)) {
                return false;
            }
        }

        return true;
    }

    public static void cancelQuest(Player player, String questId) {
        QuestProgress progress = player.getQuestProgresses().get(questId);

        if(progress == null || progress.isComplete()) return;

        String reason = progress.tryCancelOtherwiseReason(player);
        if(reason != null) {
            player.showDialog(DialogHelper.messageDialog("Cannot Cancel Quest", reason));
            return;
        }

        player.getQuestProgresses().remove(questId);
        sendPlayerCancelQuestMessage(player, questId);
    }

    public static void finishQuest(Player player, Quest quest) {
        QuestProgress progress = player.getQuestProgresses().get(quest.getId());

        if(progress.isComplete()) return;

        for(QuestTask task : quest.getTasks()) {
            if(task.getCollectInventory() != null) {
                task.getCollectInventory().removeFromPlayer(player);
            }
        }

        for(int i = 0; i < quest.getTasks().size(); i++) {
            progress.getTaskProgresses().set(i, quest.getTasks().get(i).getQuantity());
        }

        quest.getReward().reward(player);

        progress.markAsComplete();

        sendPlayerQuestMessage(player, progress);
        // TODO: this should normally happen without the player having to return to the android
        performAction(player, quest, QuestAction.Type.DONE);
    }

    public static void performAction(Player player, Quest quest, QuestAction.Type actionType) {
        if(quest.getActions() == null) return;

        List<QuestAction> actions = quest.getActions().get(actionType);

        if(actions == null) return;

        for(QuestAction action : actions) {
            action.performAction(player);
        }
    }

    public static void sendInitialPlayerQuestMessages(Player player) {
        Map<String, QuestProgress> progresses = player.getQuestProgresses();

        if(progresses == null) return;
        
        for(QuestProgress progress : progresses.values()) {
            sendPlayerQuestMessage(player, progress);
        }
    }

    public static void sendPlayerQuestMessage(Player player, QuestProgress progress) {
        Quest quest = Quests.get(progress.getQuestId());

        if(quest == null) return;

        // TODO: detect mobile player properly
        if(player.isV3()) {
            player.sendMessage(new QuestMessage(quest.getPcDetails(), progress.getClientStatus()));
        } else {
            player.sendMessage(new QuestMessage(quest.getMobileDetails(), progress.getClientStatus()));
        }
        
    }

    public static void sendPlayerCancelQuestMessage(Player player, String questId) {
        player.sendMessage(new QuestMessage(MapHelper.map("id", questId), null));
    }

}
