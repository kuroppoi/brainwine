package brainwine.gameserver.command;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.order.Order;
import brainwine.gameserver.order.OrderManager;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.server.messages.EventMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandInfo(name = "order", description = "Shows a prompt where you can update your displayed order icon.")
public class OrderCommand extends Command {
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.notify("Only players can update their order icon.", NotificationType.SYSTEM);
        }
        Player player = (Player)executor;

        List<String> orderKeys = player.getOrders().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        final Map<Order, Integer> orders = OrderManager.getOrders(player);
        List<String> options = orders.keySet().stream().map(Order::getTitle).collect(Collectors.toList());
        options.add("None");

        Dialog dialog = new Dialog()
                .setTitle("Change the Order displayed by your name")
                .addSection(new DialogSection()
                        .setInput(new DialogSelectInput().setOptions(options).setKey("order"))
                );

        player.showDialog(dialog, ans -> {
            if(ans.length == 0 || "cancel".equals(ans[0]) || !(ans[0] instanceof String)) return;
            String newValue = "None".equals(ans[0]) ? null : (String)ans[0];
            player.setDisplayedOrder(OrderManager.getOrderKeyFromTitle(newValue));
            player.sendMessage(new EventMessage("playerIconDidChange", player.getIconEmoji()));
            player.sendMessageToPeers(new EntityChangeMessage(player.getId(), player.getStatusConfig()));
        });
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/order";
    }
}
