package brainwine.gameserver.order;

import brainwine.gameserver.entity.EntityGroup;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.player.Player;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class OrderTier {
    @JsonProperty
    private Map<String, Integer> requirements;

    public boolean satisfies(Player player) {
        for(String requirement : requirements.keySet()) {
            int compare = Integer.MAX_VALUE;
            switch(requirement) {
                case "level":
                    compare = player.getLevel();
                    break;
                case "progress/chunks explored":
                    compare = player.getStatistics().getAreasExplored();
                    break;
                case "progress/teleporters discovered":
                    compare = player.getStatistics().getTeleporterDiscoveries();
                    break;
                case "progress/creatures killed":
                    compare = player.getStatistics().getTotalKills();
                    break;
                case "players_killed":
                    // TODO: related to KillerAchievement
                    break;
                case "progress/dungeons raided":
                    compare = player.getStatistics().getDungeonsRaided();
                    break;
                case "progress/automata killed":
                    compare = player.getStatistics().getKills(EntityGroup.AUTOMATA);
                    break;
                case "progress/supernatural killed":
                    compare = player.getStatistics().getKills(EntityGroup.SUPERNATURAL);
                    break;
                case "progress/inhibitors activated":
                    // TODO: related to InsurrectionAchievement
                    break;
                case "progress/brains killed":
                    compare = player.getStatistics().getKills(EntityGroup.BRAINS);
                    break;
                case "brain_lords_killed":
                    compare = player.getStatistics().getKills(EntityRegistry.getEntityConfig("brains/large"));
                    break;
            }

            if(compare < requirements.get(requirement)) {
                return false;
            }
        }

        return true;
    }
}
