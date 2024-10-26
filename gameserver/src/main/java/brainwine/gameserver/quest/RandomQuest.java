package brainwine.gameserver.quest;

import java.util.Random;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.randomquests.*;
import brainwine.gameserver.util.randomobject.RandomListObjectMapperProvider;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Kill.class, name = "kill"),
        @JsonSubTypes.Type(value = Collect.class, name = "collect"),
})
@RandomListObjectMapperProvider(RandomQuests.MapperProvider.class)
public abstract class RandomQuest {
    private String type = "none";
    private int tier = 1;
    private int frequency = 1;

    public abstract void nextQuest(Random random, Player player, Quest quest);

    public String getType() {
        return type;
    }

    public int getTier() {
        return tier;
    }

    public int getFrequency() {
        return frequency;
    }

}
