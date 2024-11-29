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
import java.util.stream.Collectors;

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
                        Arrays.asList("craft", "code", item == null ? 0 : item.getCode())
                ));
                task.setQuantity(amount);
                tasks.add(task);
            }

            quest.setTitle("Craft Items");
            quest.setTasks(tasks);
            quest.setReward(RandomQuestReward.nextOrDefault(random, getReward()));

            quest.setStory(new QuestStory()
                    .setIntro("I ran out of well-thought quests so I am just tasking you to craft the following items: \n" +
                            tasks.stream().map(QuestTask::getDescription).map(s -> "- " + s + "\n").collect(Collectors.joining()) +
                            "Sounds good?"
                    )
                    .setAccept("Alright")
                    .setBegin("OK then. Good luck!")
                    .setIncomplete("You still haven't crafted all the items.")
                    .setComplete("Good job! You are getting a reward your hard work.\nHope to see you again!")
            );

            return quest;
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
