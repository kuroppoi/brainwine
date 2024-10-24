package brainwine.gameserver.quest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.player.Player;

public class QuestReward {
    @JsonProperty(required = false)
    private Integer xp = null;
    @JsonProperty(required = false)
    private Integer crowns = null;
    @JsonProperty(value = "loot_categories", required = false)
    private List<String> lootCategories;

    public void reward(Player player) {
        if(xp != null) {
            player.addCrowns(crowns);
        }
        if(crowns != null) {
            player.addExperience(xp, String.format("You have gained %d XP from completing this quest!", xp, crowns));
        }
        if(lootCategories != null) {
            Loot loot = GameServer.getInstance().getLootManager().getRandomLoot(player, lootCategories);
            if(loot != null) {
                player.awardLoot(loot);
            }
        }
    }

    public Integer getXp() {
        return xp;
    }

    public Integer getCrowns() {
        return crowns;
    }

    public List<String> getLootCategories() {
        return lootCategories;
    }

    public QuestReward setXp(Integer xp) {
        this.xp = xp;
        return this;
    }

    public QuestReward setCrowns(Integer crowns) {
        this.crowns = crowns;
        return this;
    }

    public QuestReward setLootCategories(List<String> lootCategories) {
        this.lootCategories = lootCategories;
        return this;
    }
}
