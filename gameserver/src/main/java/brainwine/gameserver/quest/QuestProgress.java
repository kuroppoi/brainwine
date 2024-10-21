package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;

public class QuestProgress {
    @JsonProperty("quest_id")
    private String questId;
    @JsonProperty("tasks")
    @JsonInclude(Include.NON_NULL)
    private List<Integer> taskProgresses;
    @JsonProperty("completed_at")
    @JsonInclude(Include.NON_NULL)
    private Long completedAt = null;

    public QuestProgress() {}
    public QuestProgress(String questId, List<Integer> taskProgresses) {
        this.questId = questId;
        this.taskProgresses = taskProgresses;
    }

    @JsonIgnore
    public Quest getQuest() {
        return Quests.get(getQuestId());
    }

    public int getTaskProgress(int index) {
        if(index < 0 || index >= getTaskProgresses().size()) {
            return 0;
        }

        return getTaskProgresses().get(index);
    }

    public String getQuestId() {
        return questId;
    }

    public List<Integer> getTaskProgresses() {
        return taskProgresses;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public String getActionChoice(String action) {
        return String.format("quest.%s.%s", questId, action);
    }

    @JsonIgnore
    public boolean isComplete() {
        return completedAt != null;
    }

    public void markAsComplete() {
        completedAt = System.currentTimeMillis();
    }

    public List<DialogSection> getDialogSection(Player player, boolean canFinishQuest) {
        List<DialogSection> result = new ArrayList<>();
        DialogSection mainSection = new DialogSection();
        result.add(mainSection);

        Quest quest = getQuest();

        if(quest == null) {
            mainSection.setTitle("QUEST NOT FOUND");
            
            return result;
        }

        mainSection.setTitle(quest.getTitle());

        if(PlayerQuests.canFinishQuest(player, quest)) {
            mainSection.addItem(new DialogListItem().setImage("shop/premium").setText("All tasks done. Visit a quester android to claim your reward!"));
        }

        for(int i = 0; i < quest.getTasks().size(); i++) {
            result.add(quest.getTasks().get(i).getDialogSection(getTaskProgress(i)));
        }

        if(canFinishQuest) {
            result.add(new DialogSection().setText("Finish Quest").setChoice(getActionChoice("finish")));
        }

        DialogSection cancelSection = new DialogSection().setChoice(getActionChoice("cancel"));

        if(player.isV3()) {
            cancelSection.setText("<color=#ff0000>Cancel Quest</color>");
        } else {
            cancelSection.setText("Cancel Quest").setTextColor("#ff0000");
        }

        result.add(cancelSection);

        return result;
    }

    @JsonIgnore
    public Map<String, Object> getClientStatus() {
        Map<String, Object> result = new HashMap<>();
        Quest quest = Quests.get(getQuestId());

        List<Integer> completedIndices = new ArrayList<>();

        for(int i = 0; i < quest.getTasks().size(); i++) {
            QuestTask task = quest.getTasks().get(i);
            if(task.getQuantity() <= getTaskProgress(i)) {
                completedIndices.add(i);
            }
        }

        result.put("progress", completedIndices);
        result.put("complete", getCompletedAt() != null);
        result.put("active", true);

        return result;
    }

    /**Revert all the progress, or return a string reason if this will fail.
     * 
     * @param player player to cancel the quest for
     * @return null if the cancellation succeeded, or a string if there is a problem
     */
    public String tryCancelOtherwiseReason(Player player) {
        if(player.isGodMode()) return null;
        
        Quest quest = getQuest();
        if(quest == null) return null;

        String reason = null;
        int count = 0;
        for(QuestAction.Type action : new QuestAction.Type[] { QuestAction.Type.BEGIN, QuestAction.Type.INTERACT }) {
            if(quest.getActions().containsKey(action)) {
                if(reason == null) {
                    reason = action.toString();
                } else {
                    reason += ", " + action.toString();
                }
            }

            count++;
        }

        if(reason == null) {
            return null;
        } else {
            return "Cannot cancel because the quest has the " + reason + (count == 1 ? " action." : " actions.");
        }
    }

}
