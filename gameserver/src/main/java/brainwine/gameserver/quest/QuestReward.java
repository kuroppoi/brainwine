package brainwine.gameserver.quest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.player.Player;

public class QuestReward {
    @JsonProperty(required = false)
    private int xp = 0;
    @JsonProperty(required = false)
    private int crowns = 0;
    @JsonProperty(value = "loot_categories", required = false)
    private List<String> lootCategories;

    public void reward(Player player) {
        if(crowns != 0) {
            player.addCrowns(crowns);
        }
        if(xp != 0) {
            player.addExperience(xp, String.format("You have gained %d XP from completing this quest!", xp));
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
