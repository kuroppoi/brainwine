package brainwine.gameserver.quest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.item.LazyItemGetter;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.Pair;

public class QuestTaskCollectInventory {
    List<Pair<LazyItemGetter, Integer>> requirements;

    @JsonCreator
    private QuestTaskCollectInventory(Map<String, Integer> inp) {
        requirements = inp.entrySet().stream()
                .map(e -> new Pair<>(new LazyItemGetter(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<Pair<LazyItemGetter, Integer>> getRequirements() {
        return requirements;
    }

    public boolean playerSatisfies(Player player) {
        if(player == null) {
            return false;
        }

        for(Pair<LazyItemGetter, Integer> req : requirements) {
            if(!player.getInventory().hasItem(req.getFirst().get(), req.getLast())) {
                return false;
            }
        }

        return true;

    }

    public void removeFromPlayer(Player player) {
        if(player == null) {
            return;
        }

        for(Pair<LazyItemGetter, Integer> req : requirements) {
            player.getInventory().removeItem(req.getFirst().get(), req.getLast());
        }
    }

    public void addDialogListItems(DialogSection section) {
        for(Pair<LazyItemGetter, Integer> req : getRequirements()) {
            section.addItem(
                new DialogListItem()
                    .setItem(req.getFirst().get().getCode()))
                    .setText(req.getFirst().get().getTitle());
        }
    }

}
