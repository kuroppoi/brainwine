package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.QuestMessage;

public class PlayerQuests {
    private PlayerQuests() {}

    public static void deleteUnknownQuestProgress(Player player) {
        if(player.getQuestProgresses() == null) return;

        for(String questId : new ArrayList<>(player.getQuestProgresses().keySet())) {
            if(Quests.get(player, questId) == null) {
                player.getQuestProgresses().remove(questId);
            }
        }

    }

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

        if(quest.getId().contains("random")) {
            player.getAndroidQuests().put(quest.getId(), quest);
        }

        player.notify("Quest has started! Use the /quests command to view your progress at any time.");
        sendPlayerQuestMessage(player, progress);
        performAction(player, quest, QuestAction.Type.BEGIN, false);
    }

    public static boolean canFinishQuest(Player player, Quest quest, boolean needToReturn) {
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
            int currentQuantity = player.getQuestProgresses().get(quest.getId()).getTaskProgress(i);

            QuestTask task = quest.getTasks().get(i);

            if(!needToReturn && task.isReturnTask()) continue;

            if(!task.checkComplete(player, currentQuantity) || !task.checkCollectInventory(player)) {
                return false;
            }
        }

        return true;
    }

    public static void cancelQuest(Player player, String questId) {
        cancelQuest(player, questId, false);
    }

    public static void cancelQuest(Player player, String questId, boolean privileged) {
        QuestProgress progress = player.getQuestProgresses().get(questId);

        if(!privileged && (progress == null || progress.isComplete())) return;

        String reason = privileged ? null : progress.getCannotCancelReason(player);
        if(reason != null) {
            player.showDialog(DialogHelper.messageDialog("Cannot Cancel Quest", reason));
            return;
        }

        player.getQuestProgresses().remove(questId);
        sendPlayerCancelQuestMessage(player, progress);
    }

    public static void finishQuest(Player player, Quest quest) {
        QuestProgress progress = player.getQuestProgresses().get(quest.getId());

        if(progress.isComplete()) return;

        for(int i = 0; i < quest.getTasks().size(); i++) {
            QuestTask task = quest.getTasks().get(i);

            progress.setTaskProgress(i, task.getQuantity());

            if(task.getCollectInventory() != null) {
                task.getCollectInventory().removeFromPlayer(player);
            }
        }

        for(int i = 0; i < quest.getTasks().size(); i++) {
            progress.setTaskProgress(i, quest.getTasks().get(i).getQuantity());
        }

        quest.getReward().reward(player);
        progress.markAsComplete();
        QuestEvents.handleCompleteQuest(player);
        sendPlayerQuestMessage(player, progress);

        // get rid of the quest if it was randomly generated
        if(quest.getId().contains("random")) {
            player.getAndroidQuests().remove(quest.getId());
            player.getQuestProgresses().remove(quest.getId());
        }

    }

    public static DialogSection performAction(Player player, Quest quest, QuestAction.Type actionType, boolean preventMutations) {
        if(quest.getActions() == null) return null;

        List<QuestAction> actions = quest.getActions().get(actionType);

        if(actions == null) return null;

        DialogSection result = null;
        for(QuestAction action : actions) {
            DialogSection newSection = action.performAction(player, preventMutations);
            if(newSection != null) result = newSection;
        }

        return result;
    }

    public static void handleQuestFinalReturn(Player player, Quest quest) {
        // if can't finish even with the return task done, set the return task progress to the previous value
        if(!canFinishQuest(player, quest, false)) {
            return;
        }

        QuestProgress progress = player.getQuestProgresses().get(quest.getId());
        if(progress == null) return;

        for(int i = 0; i < quest.getTasks().size(); i++) {
            QuestTask task = quest.getTasks().get(i);

            if(task.isReturnTask()) {
                progress.setTaskProgress(i, quest.getTasks().get(i).getQuantity());
            }
        }
    }

    public static void sendInitialPlayerQuestMessages(Player player) {
        Map<String, QuestProgress> progresses = player.getQuestProgresses();

        if(progresses == null) return;

        for(QuestProgress progress : progresses.values()) {
            sendPlayerQuestMessage(player, progress);
        }

        List<Quest> dailyQuests = player.getDailyQuest() == null ? null : player.getDailyQuest().getValue();
        if(player.getDailyQuest() != null && !player.getDailyQuest().isExpired() && dailyQuests != null) {
            DailyQuests.sendDailyQuestTime(player, player.getDailyQuest().getTimeUntilExpiry(System.currentTimeMillis()));
        }
    }

    public static void sendPlayerQuestMessage(Player player, QuestProgress progress) {
        if(progress == null) return;

        Quest quest = Quests.get(player, progress.getQuestId());

        if(quest == null) return;

        // TODO: detect mobile player properly
        Map<String, Object> details = new HashMap<>(player.isV3() ? quest.getPcDetails() : quest.getMobileDetails());

        List<String> taskDescriptions = (List<String>)details.get("tasks");
        for(int i = 0; i < taskDescriptions.size(); i++) {
            QuestTask task = i < quest.getTasks().size() ? quest.getTasks().get(i) : null;
            int wantedProgress = task.getQuantity();
            int currentProgress = progress.getTaskProgress(i);
            if(wantedProgress > 1) {
                taskDescriptions.set(0, String.format("%s, (Progress: %d/%d)", taskDescriptions.get(i), currentProgress, wantedProgress));
            }
        }

        player.sendMessage(new QuestMessage(details, progress.getClientStatus(player)));
    }

    public static void sendPlayerCancelQuestMessage(Player player, Quest current) {
        if(player.getQuestProgresses() == null || current == null) return;
        QuestProgress progress = player.getQuestProgresses().get(current.getId());
        if(progress != null) sendPlayerCancelQuestMessage(player, progress);
    }

    public static void sendPlayerCancelQuestMessage(Player player, QuestProgress progress) {
        if(progress == null) return;
        Quest quest = progress.getQuest(player);

        Map<String, Object> pcDetails = new HashMap<>();
        pcDetails.put("id", progress.getQuestId());
        pcDetails.put("group", "Cancelled");
        pcDetails.put("title", quest == null || quest.getTitle() == null ? "Cancelled Quest" : quest.getTitle());
        pcDetails.put("xp", 0);
        pcDetails.put("desc", quest == null || quest.getDescription() == null ? "This quest has been cancelled. Disconnect and rejoin to make it disappear." : quest.getDescription());

        progress.getClientStatus(player);
        player.sendMessage(new QuestMessage(pcDetails, progress.getClientStatus(player)));
    }

}
