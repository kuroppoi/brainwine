package brainwine.gameserver.quest.randomquests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import org.apache.commons.text.WordUtils;

public class Kill extends RandomQuest {
    @JsonProperty("categories")
    @RandomListItemType(String.class)
    private RandomList<String> categories = null;
    @JsonProperty("codes")
    @RandomListItemType(RandomInteger.class)
    private RandomList<RandomInteger> codes = null;
    @JsonProperty("entities")
    @RandomListItemType(String.class)
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
    @JsonProperty("actions")
    @RandomListItemType(String.class)
    private RandomList<String> actions = new ConstantList<>(Arrays.asList("kill", "explode"));

    private List<List<Object>> getKillEvents(List<String> actions, String type, List<?> values) {
        List<List<Object>> events = new ArrayList<>(actions.size() * values.size());
        for(String action : actions) {
            for(Object value : values) {
                events.add(Arrays.asList(action, type, value));
            }
        }

        return events;
    }

    private QuestTask makeTaskForEntityTypes(List<String> actions, List<String> names, List<Integer> codes, int quantity) {
        List<List<Object>> events = getKillEvents(actions, "code", codes);

        String message;
        if(taskDescription == null) {
            String actionMessage = WordUtils.capitalize(joinWithOr(actions));
            String times = quantity == 1 ? "" : " " + quantity + " times";
            String concat = joinWithOr(names);

            String beginning;
            if(names.size() == 1) beginning = Stream.of("a", "e", "i", "o", "u").anyMatch(concat::startsWith) ? " an " : " a ";
            else beginning = " any of ";

            message = actionMessage + beginning + concat + times;
        } else {
            message = taskDescription.replaceAll("\\{QUANTITY\\}", Integer.toString(quantity));
        }

        return new QuestTask().setDescription(message).setEvents(events).setQuantity(quantity);
    }

    private QuestTask makeTaskForEntityCategories(List<String> actions, List<String> categories, int quantity) {
        List<List<Object>> events = getKillEvents(actions, "category", categories);

        String message;
        if(taskDescription == null) {
            String actionMessage = WordUtils.capitalize(String.join(" or ", actions));

            String times = quantity == 1 ? "" : " " + quantity + " times";

            String concat = joinWithOr(categories);

            String beginning;
            if(categories.size() == 1) beginning = " an entity of category ";
            else beginning = " an entity of categories ";

            message = actionMessage + beginning + concat + times;
        } else {
            message = taskDescription.replaceAll("\\{QUANTITY\\}", Integer.toString(quantity));
        }

        return new QuestTask().setDescription(message).setEvents(events).setQuantity(quantity);
    }

    @Override
    public Quest nextQuest(Random random, Player player) {
        try {
            Quest quest = new Quest();
            quest.setDescription(RandomQuests.getString(random, "kill_description"));

            List<QuestTask> tasks = new ArrayList<>();
            List<String> actions = this.actions.next(random);
            String titleActionMessage = String.join(" or ", actions.stream().map(WordUtils::capitalize).collect(Collectors.toList()));
            String title = titleActionMessage;

            if(categories != null) {
                List<String> values = categories.next(random);
                int quantity = defaultIfNull(categoryQuantity, this.quantity).next(random);
                tasks.add(makeTaskForEntityCategories(actions, values, quantity));
                title = titleActionMessage + " " + joinWithOr(values);
            }

            if(entityIds != null) {
                List<String> values = entityIds.next(random);
                int quantity = defaultIfNull(entityIdQuantity, this.quantity).next(random);
                List<String> names = new ArrayList<>();
                List<Integer> codes = new ArrayList<>();
                for(String id : values) {
                    EntityConfig config = EntityRegistry.getEntityConfig(id);

                    if(config == null) {
                        names.add(id);
                        codes.add(0);
                        continue;
                    }

                    names.add(defaultIfNull(config.getTitle(), id));
                    codes.add(config.getType());
                }

                tasks.add(makeTaskForEntityTypes(actions, names, codes, quantity));
                title = titleActionMessage + " " + joinWithOr(names);
            }

            if(codes != null) {
                List<Integer> values = codes.next(random).stream().map(ri -> ri.next(random)).collect(Collectors.toList());
                int quantity = defaultIfNull(codeQuantity, this.quantity).next(random);
                List<String> names = new ArrayList<>();
                List<Integer> codes = new ArrayList<>();
                for(Integer code : values) {
                    names.add("(Type: " + code + ")");
                    codes.add(code);
                }

                tasks.add(makeTaskForEntityTypes(actions, names, codes, quantity));
                title = titleActionMessage + " " + joinWithOr(names);
            }

            quest.setTitle(title);
            quest.setTasks(tasks);
            quest.setReward(RandomQuestReward.nextOrDefault(random, getReward()));

            quest.setStory(new QuestStory()
                    .setIntro("You gotta be killing: \n" +
                            tasks.stream().map(QuestTask::getDescription).map(s -> "- " + s + "\n").collect(Collectors.joining()) +
                            "Sounds good?"
                    )
                    .setAccept("Positive")
                    .setBegin("OK then. Good luck!")
                    .setIncomplete("You still haven't killed all the necessary entities.")
                    .setComplete("Good job! You are getting a reward your hard work.\nHope to see you again!")
            );

            return quest;
        } catch (ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Kill{ ");

        sb.append("taskDescription='" + taskDescription + '\'' +
                ", reward=" + getReward() +
                ", actions=" + actions);

        if(categories != null) {
            sb.append(", categories=" + categories + ", categoryQuantity=" + categoryQuantity);
        }
        if(entityIds != null) {
            sb.append(", entityIds=" + entityIds + ", entityIdQuantity=" + entityIdQuantity);
        }
        if(codes != null) {
            sb.append(", codes=" + codes + ", codeQuantity=" + codeQuantity);
        }

        sb.append(" }");

        return sb.toString();
    }
}
