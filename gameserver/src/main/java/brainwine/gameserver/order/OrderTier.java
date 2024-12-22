package brainwine.gameserver.order;

import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.EntityGroup;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.player.Player;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderTier {
    @JsonProperty
    private Map<String, Integer> requirements = new HashMap<>();

    private int getPlayerStat(Player player, String requirement) {
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
            case "crowns_spent":
                compare = player.getStatistics().getCrownsSpent();
                break;
        }

        return compare;
    }

    public boolean satisfies(Player player) {
        for(String requirement : requirements.keySet()) {
            if(getPlayerStat(player, requirement) < requirements.get(requirement)) {
                return false;
            }
        }

        return true;
    }

    private String joinWithAnd(List<String> strings) {
        if(strings.isEmpty()) return "nothing";
        if(strings.size() < 2) return strings.stream().collect(Collectors.joining(" and "));

        return strings.stream().limit(strings.size() - 1).collect(Collectors.joining(", "))
                + " and " + strings.get(strings.size() - 1);
    }

    public void addDialogItems(Player player, DialogSection section) {
        List<String> strings = new ArrayList<>();
        for(Map.Entry<String, Integer> e : requirements.entrySet()) {
            String requirement = e.getKey();
            int requiredProgress = e.getValue();
            int progress = getPlayerStat(player, requirement);

            if(progress >= requiredProgress) continue;

            String format = OrderManager.getStatFormat(requirement);
            strings.add(format.replace("%d", Integer.toString(requiredProgress - progress)));
        }

        section.setText(StringUtils.capitalize(joinWithAnd(strings)) + " until the next rank of your order!");
    }
}
