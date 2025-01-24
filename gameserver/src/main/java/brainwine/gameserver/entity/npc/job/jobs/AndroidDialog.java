package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.PlayerQuests;
import brainwine.gameserver.quest.Quest;
import brainwine.gameserver.quest.QuestAction;
import brainwine.gameserver.quest.QuestProgress;
import brainwine.gameserver.quest.QuestTask;

import java.util.List;

public class AndroidDialog extends DialoguerJob {

    @Override
    public DialogSection getMainDialogSection(Npc me, Player player) {
        for(QuestProgress progress : player.getQuestProgresses().values()) {
            if(progress.isComplete()) continue;
            Quest quest = progress.getQuest(player);
            if(quest == null) return null;

            // Find quest task that requires talking to this android
            int taskIndex = -1;
            for(int i = 0; i < quest.getTasks().size(); i++) {
                QuestTask task = quest.getTasks().get(i);
                if(task.getEvents() != null) for(List<Object> event : task.getEvents()) {
                    if(event.size() >= 3
                            && "interact".equals(event.get(0))
                            && "name".equals(event.get(1))
                            && me.getName() != null
                            && me.getName().equals(event.get(2))
                    ) {
                        taskIndex = i;
                        break;
                    }
                }
            }
            if(taskIndex < 0) continue;

            QuestTask task = quest.getTasks().get(taskIndex);

            // Perform the INTERACT action, only doing the mutations that give the player
            // an advantage if this is the first interaction after the task is first received.
            boolean preventMutations = task.checkComplete(player, progress.getTaskProgress(taskIndex));

            // TODO: This is hacked in.
            return PlayerQuests.performAction(player, quest, QuestAction.Type.INTERACT, preventMutations);
        }

        return new DialogSection().setText("I don't know what to say.");
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Player player, Object[] ans) {
        return true;
    }
}
