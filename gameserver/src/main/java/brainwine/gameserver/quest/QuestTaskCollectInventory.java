package brainwine.gameserver.quest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.Pair;

public class QuestTaskCollectInventory {
    List<Pair<String, Integer>> requirements;
    @JsonCreator
    public QuestTaskCollectInventory(Map<String, Integer> inp) {
        requirements = inp.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<Pair<String, Integer>> getRequirements() {
        return requirements;
    }

    public QuestTaskCollectInventory setRequirements(List<Pair<String, Integer>> requirements) {
        this.requirements = requirements;
        return this;
    }

    public boolean check(Player player) {
        if(player == null) {
            return false;
        }

        for(Pair<String, Integer> req : requirements) {
            if(!player.getInventory().hasItem(ItemRegistry.getItem(req.getFirst()), req.getLast())) {
                return false;
            }
        }

        return true;

    }

    public void removeFromPlayer(Player player) {
        if(player == null) {
            return;
        }

        for(Pair<String, Integer> req : requirements) {
            player.getInventory().removeItem(ItemRegistry.getItem(req.getFirst()), req.getLast(), true);
        }
    }

    public void addDialogListItems(DialogSection section) {
        for(Pair<String, Integer> req : getRequirements()) {
            Item item = ItemRegistry.getItem(req.getFirst());
            if(!item.isAir()) section.addItem(
                new DialogListItem()
                    .setItem(item.getCode()))
                    .setText(item.getTitle());
        }
    }

}
