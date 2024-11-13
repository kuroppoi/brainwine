package brainwine.gameserver.quest.randomquests;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.ConcretionFailureException;
import brainwine.gameserver.util.randomobject.RandomInteger;
import brainwine.gameserver.util.randomobject.RandomList;
import brainwine.gameserver.util.randomobject.RandomListItemType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Craft extends RandomQuest {
    @JsonProperty("items")
    @RandomListItemType(CraftItem.class)
    private RandomList<CraftItem> items;

    private static class CraftItem {
        @JsonProperty
        String item = Item.AIR.getId();
        @JsonProperty
        RandomInteger count = new RandomInteger(1);
    }

    @Override
    public Quest nextQuest(Random random, Player player) {
        try {
            Quest quest = new Quest();
            quest.setDescription(RandomQuests.getString(random, "craft_description"));

            List<CraftItem> chosenItems = items.next(random);

            List<QuestTask> tasks = new ArrayList<>(chosenItems.size());
            for(CraftItem craftItem : chosenItems) {
                Item item = Item.get(craftItem.item);

                String itemName = item == null || item.getTitle() == null ? craftItem.item : item.getTitle();
                int amount = craftItem.count.next(random);
                QuestTask task = new QuestTask();
                task.setDescription("Craft " + amount + " of " + itemName);
                task.setEvents(Arrays.asList(
                        Arrays.asList("craft", "code", item.getCode())
                ));
                tasks.add(task);
            }
            quest.setTasks(tasks);

            if(reward != null) quest.setReward(reward.next(random));

            return quest;
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
