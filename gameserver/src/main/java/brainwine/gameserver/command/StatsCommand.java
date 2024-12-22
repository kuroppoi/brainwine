package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.order.Order;
import brainwine.gameserver.order.OrderManager;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;

import java.util.Objects;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "stats", description = "Lists your stats and progress needed to advance into orders.")
public class StatsCommand extends Command {
    private void showStats(Player executor, Player target) {
        Dialog dialog = new Dialog()
                .setTitle(executor == target ? "Your Order Progress" : target.getName() + "'s Order Progress");

        for(Order order : OrderManager.getOrders().values()) {
            dialog.addSection(order.getStatDialogSection(target));
        }

        executor.showDialog(dialog);
    }

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.notify("Only online players can check out their stats.", NotificationType.SYSTEM);
            return;
        }

        if(args.length > 0) {
            if(!executor.isAdmin() && !Objects.equals(args[0], ((Player) executor).getName())) {
                executor.notify("You are not allowed to view other player's stats.", SYSTEM);
                return;
            }

            Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);

            if(target == null) {
                executor.notify("That player does not exist.", SYSTEM);
                return;
            }

            showStats((Player)executor, target);
        } else {
            showStats((Player)executor, (Player)executor);
        }

    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/stats [player]";
    }
}
