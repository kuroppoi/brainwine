package brainwine.gameserver.entity.npc.job.jobs;

import java.util.List;
import java.util.Map;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Pair;

public class Quester extends DialoguerJob {
    @Override
    public DialogSection getMainDialogSection(Npc me, Player player) {
        return new DialogSection().setText(MapHelper.getString(GameConfiguration.getBaseConfig(), "dialogs.android.quest")).setChoice("offers");
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Player player, Object[] ans) {
        return dialogueOfferQuests(me, player);
    }

    private Pair<String, Integer> getQuestCategoryAndLevel(Npc me) {
        if (me.getName() == null) {
            return null;
        }

        String[] parts = me.getName().split(" ");

        Map<String, String> nameToCategoryTitle = MapHelper.getMap(GameConfiguration.getBaseConfig(), "quests.sources");

        if (parts.length == 1) {
            return new Pair<>(nameToCategoryTitle.get(parts[0]), 1);
        } else {
            return new Pair<>(nameToCategoryTitle.get(parts[0]), MathUtils.clamp(parts[1].length(), 1, 3));
        }

    }

    public boolean dialogueOfferQuests(Npc me, Player player) {
        HardcodedQuest hardcodedQuest = Quests.getHardcodedQuests().get(me.getName());
        if (hardcodedQuest != null) {
            return dialogueHardcodedQuest(me, player, hardcodedQuest);
        } else {
            return dialogueOtherQuest(me, player);
        }
    }

    public boolean dialogueHardcodedQuest(Npc me, Player player, HardcodedQuest hardcodedQuest) {
        String questId = hardcodedQuest.getQuestId();

        QuestProgress currentProgress = player.getQuestProgresses().get(questId);

        if (currentProgress == null) {
            dialogueConfirmBeginQuest(me, player, questId);
        } else {
            dialogueCheckProgress(me, player, questId);
        }
        return true;
    }

    public boolean dialogueOtherQuest(Npc me, Player player) {
        Map<String, Object> config = GameConfiguration.getBaseConfig();
        Pair<String, Integer> categoryAndLevel = getQuestCategoryAndLevel(me);

        if (categoryAndLevel != null) {
            String category = categoryAndLevel.getFirst();
            int level = categoryAndLevel.getLast();

            if (category != null) {
                QuestProgress currentProgress = Quests.getIncompleteQuestProgressInCategory(player, category);

                if (currentProgress == null) {
                    List<Quest> quests = Quests.getRandomQuestsFromCategory(me, category, player.getQuestProgresses().keySet(), 5);

                    PlayerQuestDialog.offerQuests(player, quests, quest -> beginQuest(me, player, quest));
                    return true;
                } else {
                    return dialogueCheckProgress(me, player, currentProgress.getQuestId());
                }
            }
        }

        player.showDialog(DialogHelper.messageDialog("No Quest Offers", MapHelper.getString(config, "dialogs.android.no_quest")));
        return true;
    }

    public boolean dialogueConfirmBeginQuest(Npc me, Player player, String questId) {
        Quest quest = Quests.get(questId);

        if (quest == null) {
            return true;
        }

        PlayerQuestDialog.offerSingleQuest(player, quest, myQuest -> beginQuest(me, player, myQuest));

        return true;
    }

    public void beginQuest(Npc me, Player player, Quest quest) {
        PlayerQuests.beginQuest(player, quest);

        player.showDialog(PlayerQuestDialog.beginQuestDialogGet(player, quest));
    }

    public boolean dialogueCheckProgress(Npc me, Player player, String questId) {
        Quest quest = Quests.get(questId);

        PlayerQuests.handleQuestFinalReturn(player, quest);

        if (PlayerQuests.canFinishQuest(player, quest)) {
            player.showDialog(DialogHelper.messageDialog(quest.getStory().getComplete()));

            PlayerQuests.finishQuest(player, quest);

            return true;
        } else {
            String message = String.format(
                "%s. Cancel the quest \"%s\" using the /quests command if you want to give up on the quest.",
                quest.getStory().getIncomplete(),
                quest.getTitle()
            );

            player.showDialog(DialogHelper.messageDialog("Cannot Finish Quest Yet", message));

            return true;
        }
        
    }

}
