package brainwine.gameserver.quest.randomquests;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

public class Collect extends RandomQuest {
    @JsonProperty("items")
    private RandomList<CollectItem> items;
    @JsonProperty("reward")
    private RandomQuestReward reward = null;
    @JsonProperty("task_description")
    private String taskDescription = null;

    private class CollectItem implements Arbitrary<QuestTask> {
        RandomList<String> item = new ConstantList<>();
        RandomInteger count = new RandomInteger(1);

        @Override
        public QuestTask next(Random random) throws ConcretionFailureException {
            Map<String, Integer> requirements = new HashMap<>();

            for (String oneItem : item.toConcrete(random)) {
                requirements.put(oneItem, count.next(random));
            }

            String myTaskDescription;
            if (taskDescription == null) {
                myTaskDescription = "Collect" + requirements.entrySet().stream().map(e -> {
                    Item item = Item.get(e.getKey());
                    String itemTitle;
                    if (item == null) {
                        itemTitle = e.getKey();
                    } else {
                        itemTitle = item.getTitle();
                    }
                    return e.getValue() + " x " + itemTitle;
                }).collect(Collectors.joining(", "));
            } else {
                myTaskDescription = taskDescription;
            }

            return new QuestTask()
                    .setDescription(myTaskDescription)
                    .setCollectInventory(new QuestTaskCollectInventory(requirements));
        }
    }

    @Override
    public void nextQuest(Random random, Player player, Quest quest) {
        try {
            List<CollectItem> collectItemList = items.toConcrete(random);
            List<QuestTask> tasks = new ArrayList<>(collectItemList.size());
            for(CollectItem c : collectItemList) {
                tasks.add(c.next(random));
            }
            quest.setTasks(tasks);
            if(reward != null) quest.setReward(reward.next(random));
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
