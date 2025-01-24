package brainwine.gameserver.quest.randomquests;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.*;
import brainwine.gameserver.util.randomobject.ConcretionFailureException;
import brainwine.gameserver.util.randomobject.ConstantList;
import brainwine.gameserver.util.randomobject.RandomList;
import brainwine.gameserver.util.randomobject.RandomListItemType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class MultiStep extends RandomQuest {
    @JsonProperty
    @RandomListItemType(RandomQuest.class)
    private RandomList<RandomQuest> steps = new ConstantList<>();
    @JsonProperty
    private String title = null;
    @JsonProperty
    private String description = null;
    @JsonProperty
    private QuestStory story = null;

    @Override
    public Quest nextQuest(Random random, Player player) {
        try {
            List<RandomQuest> chosen = steps.next(random);
            List<Quest> quests = chosen.stream()
                    .map(rq -> rq.nextQuest(random, player))
                    .collect(Collectors.toList());

            List<QuestTask> tasks = new ArrayList<>();

            for(Quest quest : quests) {
                tasks.addAll(quest.getTasks());
            }

            Quest result = new Quest();

            if(!quests.isEmpty()) {
                Quest toBeCopied = quests.get(quests.size() - 1);
                RandomQuestReward altRandomQuestReward = chosen.get(quests.size() - 1).getReward();
                result.setDescription(defaultIfNull(description, toBeCopied.getDescription()));

                return new Quest()
                        .setTitle(defaultIfNull(title, toBeCopied.getTitle()))
                        .setDescription(defaultIfNull(description, toBeCopied.getDescription()))
                        .setTasks(tasks)
                        .setReward(RandomQuestReward.nextOrDefault(random, getReward(), altRandomQuestReward))
                        .setStory(defaultIfNull(story, toBeCopied.getStory()));
            }

            return new Quest()
                    .setTitle(title)
                    .setDescription(description)
                    .setTasks(tasks)
                    .setReward(RandomQuestReward.nextOrDefault(random, getReward()))
                    .setStory(story);

        } catch(ConcretionFailureException e) {
            throw new IllegalStateException("Concretion failure!");
        }
    }
}
