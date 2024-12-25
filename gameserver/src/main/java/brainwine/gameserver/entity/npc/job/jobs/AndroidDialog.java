package brainwine.gameserver.entity.npc.job.jobs;

import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.Quest;
import brainwine.gameserver.quest.QuestAction;
import brainwine.gameserver.quest.QuestProgress;

import java.util.List;

public class AndroidDialog extends DialoguerJob {

    @Override
    public DialogSection getMainDialogSection(Npc me, Player player) {
        for(QuestProgress progress : player.getQuestProgresses().values()) {
            if(progress.isComplete()) continue;
            Quest quest = progress.getQuest(player);
            if(quest == null) return null;

            List<QuestAction> actions = quest.getActions().get(QuestAction.Type.INTERACT);

            if(actions != null) for(QuestAction action : actions) {
                if("show_android_dialog".equals(action.getMethod())) {
                    if(action.getParams() == null
                            || action.getParams().size() < 2
                            || !(action.getParams().get(0) instanceof String)
                            || !(action.getParams().get(1) instanceof String)
                    ) continue;

                    String name = (String)action.getParams().get(1);
                    if(me.getName() != null && me.getName().startsWith(name)) {
                        return new DialogSection().setText((String)action.getParams().get(0));
                    }
                }
            }
        }

        return new DialogSection().setText("I don't know what to say.");
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Player player, Object[] ans) {
        return true;
    }
}
