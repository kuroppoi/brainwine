package brainwine.gameserver.quest;

import java.util.Random;

import brainwine.gameserver.util.randomobject.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RandomQuestReward implements Arbitrary<QuestReward> {
    @JsonProperty("xp")
    RandomInteger xp = null;
    @JsonProperty("crowns")
    RandomInteger crowns = null;
    @JsonProperty("loot_categories")
    @RandomListItemType(String.class)
    RandomList<String> lootCategories = null;
    
    @Override
    public QuestReward next(Random random) throws ConcretionFailureException {
        QuestReward result = new QuestReward();
        if(xp != null) result.setXp(xp.next(random));
        if(crowns != null) result.setCrowns(crowns.next(random));
        if(lootCategories != null) result.setLootCategories(lootCategories.next(random));

        return result;
    }
}
