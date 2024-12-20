package brainwine.gameserver.order;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Order {
    @JsonIgnore
    private String title;
    @JsonProperty("induction_message")
    private String inductionMessage;
    @JsonProperty("advancement_message")
    private String advancementMessage;
    @JsonProperty
    private String key;
    @JsonProperty
    private boolean hidden;
    @JsonProperty
    private List<OrderTier> tiers;

    public void advance(Player player) {
        int currentLevel = player.getOrders().getOrDefault(key, 0);
        boolean advanced = false;

        while(currentLevel < tiers.size()) {
            int previousLevel = currentLevel;
            currentLevel = advanceOnce(player, currentLevel);
            if(previousLevel == currentLevel) break;
            advanced = true;
        }

        if(advanced && !hidden) {
            String title, message, peerMessage;
            if(currentLevel == 1) {
                title = OrderManager.getInductionTitle();
                message = inductionMessage + " " + title;
                peerMessage = OrderManager.getPeerInductionMessage() + " " + title;
            } else {
                title = OrderManager.getAdvancementTitle();
                message = advancementMessage + " " + title;
                peerMessage = OrderManager.getPeerAdvancementMessage() + " " + title;
            }

            player.showDialog(DialogHelper.messageDialog(title, message));
            player.notifyPeers(peerMessage, NotificationType.SYSTEM);
        }
    }

    public int advanceOnce(Player player, int currentLevel) {
        if (currentLevel == tiers.size()) return currentLevel;
        OrderTier nextTier = tiers.get(currentLevel);
        player.getOrders().put(key, nextTier.satisfies(player) ? currentLevel + 1 : currentLevel);
        return player.getOrders().get(key);
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
