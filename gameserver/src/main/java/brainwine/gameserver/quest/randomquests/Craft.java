package brainwine.gameserver.quest.randomquests;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Craft extends RandomQuest {
    @JsonProperty("tasks")
    @RandomListItemType(CraftItem.class)
    private RandomList<CraftItem> tasks;
    @JsonProperty("task_description")
    private String taskDescription = null;

    private static class CraftItem {
        @JsonProperty("items")
        @RandomListItemType(String.class)
        RandomList<String> items = new ConstantList<>();
        @JsonProperty("count")
        RandomInteger count = new RandomInteger(1);
    }

    @Override
    public Quest nextQuest(Random random, Player player) {
        try {
            Quest quest = new Quest();
            quest.setDescription(RandomQuests.getString(random, "craft_description"));

            List<CraftItem> chosenItems = tasks.next(random);

            List<QuestTask> tasks = new ArrayList<>(chosenItems.size());
            for(CraftItem craftItem : chosenItems) {
                List<String> itemsToCraft = craftItem.items.next(random);
                int amount = craftItem.count.next(random);
                List<List<Object>> events = new ArrayList<>(itemsToCraft.size());
                List<String> itemNames = new ArrayList<>(itemsToCraft.size());

                for(String itemId : itemsToCraft) {
                    Item item = Item.get(itemId);

                    itemNames.add(item == null || item.getTitle() == null ? itemId : item.getTitle());
                    events.add(Arrays.asList("craft", "code", item == null ? 0 : item.getCode()));
                }

                QuestTask task = new QuestTask();
                if(taskDescription == null) {
                    task.setDescription("Craft " + amount + " of " + joinWithOr(itemNames));
                } else {
                    task.setDescription(taskDescription.replaceAll("\\{QUANTITY\\}", Integer.toString(amount)));
                }
                task.setEvents(events);
                task.setQuantity(amount);

                tasks.add(task);
            }
            quest.setTasks(tasks);
            quest.setReward(RandomQuestReward.nextOrDefault(random, getReward()));

            return quest;
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
