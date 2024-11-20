package brainwine.gameserver.quest;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.randomquests.*;
import brainwine.gameserver.util.randomobject.RandomListObjectMapperProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MultiStep.class, name = "multistep"),
        @JsonSubTypes.Type(value = Kill.class, name = "kill"),
        @JsonSubTypes.Type(value = Collect.class, name = "collect"),
        @JsonSubTypes.Type(value = Craft.class, name = "craft"),
})
@RandomListObjectMapperProvider(RandomQuests.MapperProvider.class)
public abstract class RandomQuest {
    @JsonProperty
    private String type = "none";
    @JsonProperty
    private int tier = 1;
    @JsonProperty
    private int frequency = 1;
    @JsonProperty
    private RandomQuestReward reward = null;

    @JsonProperty
    private List<RandomQuestDomain> domain = Stream.of(
            RandomQuestDomain.DAILY,
            RandomQuestDomain.ANDROID_SURVIVAL,
            RandomQuestDomain.ANDROID_COMBAT,
            RandomQuestDomain.ANDROID_COOKING,
            RandomQuestDomain.ANDROID_COLLECT
    ).collect(Collectors.toList());

    /**Randomly generate a quest according to the specification found in the class instance.
     *
     * @param random random instance to generate random values with
     * @param player player that the quest is being generated for
     * @return a quest. Implementors are not obliged to set the title of the quest, but other fields must have valid values
     */
    public abstract Quest nextQuest(Random random, Player player);

    public String getType() {
        return type;
    }

    public int getTier() {
        return tier;
    }

    public int getFrequency() {
        return frequency;
    }

    public RandomQuestReward getReward() {
        return reward;
    }

    public List<RandomQuestDomain> getDomain() {
        return domain;
    }
}
