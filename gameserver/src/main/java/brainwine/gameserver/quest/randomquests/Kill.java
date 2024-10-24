package brainwine.gameserver.quest.randomquests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.ConcretionFailureException;
import brainwine.gameserver.util.randomobject.ConstantList;
import brainwine.gameserver.util.randomobject.RandomInteger;
import brainwine.gameserver.util.randomobject.RandomList;
import com.fasterxml.jackson.annotation.JsonProperty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import org.apache.commons.text.WordUtils;

public class Kill extends RandomQuest {
    @JsonProperty("categories")
    private RandomList<String> categories = null;
    @JsonProperty("codes")
    private RandomList<RandomInteger> codes = null;
    @JsonProperty("entities")
    private RandomList<String> entityIds = null;
    @JsonProperty("quantity")
    private RandomInteger quantity = null;
    @JsonProperty("category_quantity")
    private RandomInteger categoryQuantity = new RandomInteger(1);
    @JsonProperty("code_quantity")
    private RandomInteger codeQuantity = new RandomInteger(1);
    @JsonProperty("entity_quantity")
    private RandomInteger entityIdQuantity = new RandomInteger(1);
    @JsonProperty("task_description")
    private String taskDescription = null;
    @JsonProperty("reward")
    private RandomQuestReward reward = null;
    @JsonProperty("actions")
    private RandomList<String> actions = new ConstantList<>(Arrays.asList("kill"));

    private List<List<Object>> getKillEvents(List<String> actions, String type, List<?> values) {
        List<List<Object>> events = new ArrayList<>(actions.size() * values.size());
        for (String action : actions) {
            for (Object value : values) {
                events.add(Arrays.asList(action, type, value));
            }
        }

        return events;
    }

    public void setTaskDescription(QuestTask task, String taskDescription, String actionMessage) {
        if(taskDescription == null) {
            List<List<Object>> events = task.getEvents();
            List<Object> values = events.stream().map(i -> i.get(2)).collect(Collectors.toList());
            int quantity = task.getQuantity();
            String times = quantity == 1 ? "" : quantity + " times";

            String concat;
            if(!events.isEmpty() && events.get(0).size() >= 2 && "code".equals(events.get(0).get(1))) {
                concat = String.join(", ", values.stream().map(v -> "(Type: " + v + ")").collect(Collectors.toList()));
            } else {
                concat = String.join(", ", values.stream().map(v -> (String) v).collect(Collectors.toList()));
            }

            String message;
            if (values.size() == 1) {
                message = actionMessage + " an entity " + concat + times;
            } else {
                message = actionMessage + " any of the entities " + concat + times;
            }

            task.setDescription(message);
        } else {
            task.setDescription(taskDescription.replaceAll("\\{QUANTITY\\}", Integer.toString(task.getQuantity())));
        }
    }

    @Override
    public void nextQuest(Random random, Player player, Quest quest) {
        try {
            quest.setTitle("Daily Quest");
            quest.setDescription(RandomQuests.getString(random, "kill_description"));

            List<QuestTask> tasks = new ArrayList<>();
            List<String> actions = this.actions.toConcrete(random);

            String actionMessage = WordUtils.capitalize(String.join(" or ", actions));

            if (categories != null) {
                List<String> values = categories.toConcrete(random);
                int quantity = defaultIfNull(this.quantity, categoryQuantity).next(random);
                QuestTask task = new QuestTask()
                        .setEvents(getKillEvents(actions, "category", values))
                        .setQuantity(quantity);
                setTaskDescription(task, taskDescription, actionMessage);
                tasks.add(task);
            }

            if (entityIds != null) {
                List<String> values = entityIds.toConcrete(random);
                int quantity = defaultIfNull(this.quantity, entityIdQuantity).next(random);
                QuestTask task = new QuestTask()
                        .setEvents(getKillEvents(actions, "entity", values))
                        .setQuantity(quantity);
                setTaskDescription(task, taskDescription, actionMessage);
                tasks.add(task);
            }

            if (codes != null) {
                List<Integer> values = codes.toConcrete(random).stream()
                        .map(i -> i.next(random))
                        .collect(Collectors.toList());
                int quantity = defaultIfNull(this.quantity, codeQuantity).next(random);
                QuestTask task = new QuestTask()
                        .setEvents(getKillEvents(actions, "code", values))
                        .setQuantity(quantity);
                setTaskDescription(task, taskDescription, actionMessage);
                tasks.add(task);
            }

            quest.setTasks(tasks);
            if (reward != null) quest.setReward(reward.next(random));
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }

}
