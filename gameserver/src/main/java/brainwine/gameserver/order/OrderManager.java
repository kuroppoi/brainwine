package brainwine.gameserver.order;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

public class OrderManager {
    private static Map<String, Order> orders = new HashMap<>();
    private static Map<String, Order> ordersByTitle = new HashMap<>();
    private static String inductionTitle = "You've been inducted into an Order!";
    private static String advancementTitle = "You've advanced in an Order!";
    private static String peerInductionMessage = "has been inducted into the";
    private static String peerAdvancementMessage = "has advanced within the";
    private static final Logger logger = LogManager.getLogger();

    private OrderManager() {}

    public static void loadOrders() {
        try {
            Map<String, Object> ordersMap = new HashMap<>(MapHelper.getMap(GameConfiguration.getBaseConfig(), "orders"));
            Map<String, String> common = MapHelper.getMap(ordersMap, "all");

            if (common != null) {
                inductionTitle = MapHelper.getString(common, "induction_title", inductionTitle);
                advancementTitle = MapHelper.getString(common, "advancement_title", advancementTitle);
                peerInductionMessage = MapHelper.getString(common, "peer_induction_message", peerInductionMessage);
                peerAdvancementMessage = MapHelper.getString(common, "peer_advancement_message", peerAdvancementMessage);
            }

            ordersMap.remove("all");

            ordersByTitle.putAll(JsonHelper.readValue(ordersMap, new TypeReference<Map<String, Order>>() {}));

            for(Map.Entry<String, Order> orderPair : ordersByTitle.entrySet()) {
                orderPair.getValue().setTitle(orderPair.getKey());
                orders.put(orderPair.getValue().getKey(), orderPair.getValue());
            }
        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load orders", e);
        }
    }

    public static void advance(Player player) {
        for(Order order : orders.values()) {
            order.advance(player);
        }
    }

    public static Map<String, Order> getOrders() {
        return Collections.unmodifiableMap(orders);
    }

    public static Map<Order, Integer> getOrders(Player player) {
        return player.getOrders().entrySet().stream()
                .filter(e -> e.getValue() > 0 && orders.containsKey(e.getKey()))
                .collect(Collectors.toMap(e -> orders.get(e.getKey()), Map.Entry::getValue));
    }

    public static String getOrderKeyFromTitle(String title) {
        Order order = ordersByTitle.get(title);

        return order == null ? null : order.getKey();
    }

    public static String getInductionTitle() {
        return inductionTitle;
    }

    public static String getAdvancementTitle() {
        return advancementTitle;
    }

    public static String getPeerInductionMessage() {
        return peerInductionMessage;
    }

    public static String getPeerAdvancementMessage() {
        return peerAdvancementMessage;
    }
}
