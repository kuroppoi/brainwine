package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;

public class PlayerQuestDialog {
    private PlayerQuestDialog() {}

    public static Dialog questOffersDialogGet(List<Quest> quests) {
        Dialog result = new Dialog().setTitle("My Quest Offers");

        result.addSection(new DialogSection().setText("Here are my quest offers for you:"));
        for(Quest quest : quests) {
            DialogSection section = new DialogSection().setText(quest.getTitle()).setChoice(quest.getId());

            result.addSection(section);
        }

        result.setActions("Cancel");

        return result;
    }

    public static Quest questOffersDialogGetSelectedOffer(List<Quest> quests, Object[] ans) {
        if(ans.length == 0) return null;
        if("cancel".equals(ans[0])) return null;

        else return quests.stream().filter(q -> Objects.equals(q.getId(), ans[0])).findFirst().orElse(null);
    }

    public static Dialog confirmBeginQuestDialogGet(Quest quest) {
        Dialog result = new Dialog().setTitle(quest.getTitle());

        result.addSection(new DialogSection().setText(quest.getStory().getIntro()));

        result.setActions("Cancel", quest.getStory().getAccept());

        return result;
    }

    public static Dialog beginQuestDialogGet(Player player, Quest quest) {
        return DialogHelper.messageDialog(player.isV3() ? quest.getStory().getBegin() : quest.getStory().getBeginMobile());
    }

    public static Dialog playerQuestsDialogGet(Player player) {
        Dialog result = new Dialog().setTitle("Your Quests");

        List<DialogSection> all = getPlayerQuestsSection(player, false);

        for(DialogSection section : all) {
            result.addSection(section);
        }

        return result;
    }

    public static void playerQuestsDialogHandle(Player player, Object[] ans) {
        if(ans.length > 0 && "cancel".equals(ans[0])) return;

        if(ans.length == 0 || !(ans[0] instanceof String)) return;

        String[] args = ((String) ans[0]).split("\\.");

        if("quest".equals(args[0])) {
            if(args.length >= 3 && "cancel".equals(args[2])) {
                PlayerQuests.cancelQuest(player, args[1]);
            }
        }
    }

    public static List<DialogSection> getPlayerQuestsSection(Player player, boolean canFinishQuest) {
        List<DialogSection> result = new ArrayList<>();
        for(QuestProgress questProgress : player.getQuestProgresses().values()) {
            if(!questProgress.isComplete()) {
                result.addAll(questProgress.getDialogSection(player, canFinishQuest));
            }
        }

        return result;
    }

    /* DRIVER FUNCTIONS */

    public static void offerQuests(Player player, List<Quest> quests, Consumer<Quest> onSelect) {
        player.showDialog(questOffersDialogGet(quests), ans -> {
            Quest quest = questOffersDialogGetSelectedOffer(quests, ans);

            if(quest != null) {
                offerSingleQuest(player, quest, onSelect);
            }
        });
    }

    public static void offerSingleQuest(Player player, Quest quest, Consumer<Quest> onSelect) {
        player.showDialog(confirmBeginQuestDialogGet(quest), ans -> {
            if(ans.length < 1 || "cancel".equals(ans[0])) return;
            onSelect.accept(quest);
        });
    }

    public static void showPlayerQuests(Player player) {
        player.showDialog(playerQuestsDialogGet(player), ans -> playerQuestsDialogHandle(player, ans));
    }

}
