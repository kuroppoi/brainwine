package brainwine.gameserver.quest;

import java.util.Random;

import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RandomQuestReward implements Arbitrary<QuestReward> {
    @JsonProperty("xp")
    private RandomInteger xp = null;
    @JsonProperty("crowns")
    private RandomInteger crowns = null;
    @JsonProperty("loot_categories")
    @RandomListItemType(String.class)
    private RandomList<String> lootCategories = null;

    public RandomQuestReward() {}

    public RandomQuestReward(RandomInteger xp, RandomInteger crowns, RandomList<String> lootCategories) {
        this.xp = xp;
        this.crowns = crowns;
        this.lootCategories = lootCategories;
    }

    @Override
    public QuestReward next(Random random) throws ConcretionFailureException {
        QuestReward result = new QuestReward();
        if(xp != null) result.setXp(xp.next(random));
        if(crowns != null) result.setCrowns(crowns.next(random));
        if(lootCategories != null) result.setLootCategories(lootCategories.next(random));

        return result;
    }
}
