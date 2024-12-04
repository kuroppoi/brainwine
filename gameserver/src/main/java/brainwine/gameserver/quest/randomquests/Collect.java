package brainwine.gameserver.quest.randomquests;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

public class Collect extends RandomQuest {
    @JsonProperty("tasks")
    @RandomListItemType(CollectItem.class)
    private RandomList<CollectItem> tasks;
    @JsonProperty("task_description")
    private String taskDescription = null;

    @RandomListObjectMapperProvider(RandomQuests.MapperProvider.class)
    private static class CollectItem {
        @JsonProperty("items")
        @RandomListItemType(String.class)
        RandomList<String> items = new ConstantList<>();
        @JsonProperty("count")
        RandomInteger count = new RandomInteger(1);
    }

    private QuestTask nextQuestTask(Random random, CollectItem collectItem) throws ConcretionFailureException {
        List<List<Object>> events = new ArrayList<>();

        for(String oneItem : collectItem.items.next(random)) {
            events.add(Arrays.asList("collect_item", "id", oneItem));
        }

        int quantity = collectItem.count.next(random);
        String myTaskDescription;
        if(taskDescription == null) {
            myTaskDescription = "Collect " + quantity + " x " + events.stream().map(e -> {
                String itemId = (String) e.get(2);
                Item item = Item.get(itemId);
                String itemTitle;
                if(item == null) {
                    itemTitle = itemId;
                } else {
                    itemTitle = item.getTitle() == null ? itemId : item.getTitle();
                }
                return itemTitle;
            }).collect(Collectors.joining(" or "));
        } else {
            myTaskDescription = taskDescription.replaceAll("\\{QUANTITY\\}", Integer.toString(quantity));
        }

        return new QuestTask()
                .setDescription(myTaskDescription)
                .setEvents(events)
                .setQuantity(quantity);
    }

    @Override
    public Quest nextQuest(Random random, Player player) {
        try {
            Quest quest = new Quest();

            quest.setDescription(RandomQuests.getString(random, "collect_description"));
            List<CollectItem> collectItemList = tasks.next(random);
            List<QuestTask> tasks = new ArrayList<>(collectItemList.size());
            for(CollectItem c : collectItemList) {
                tasks.add(nextQuestTask(random, c));
            }
            quest.setTasks(tasks);
            quest.setReward(RandomQuestReward.nextOrDefault(random, getReward()));

            return quest;
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
