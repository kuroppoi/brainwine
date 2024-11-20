package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.PlayerQuestDialog;

import java.util.Objects;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "quests", description = "Lists your ongoing and completed quests.")
public class QuestsCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.notify("You can only view your quests as a player!", NotificationType.SYSTEM);
        }

        if(args.length > 0) {
            if(!executor.isAdmin() && !Objects.equals(args[0], ((Player) executor).getName())) {
                executor.notify("You are not allowed to view other player's quests.", SYSTEM);
                return;
            }

            Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);

            if(target == null) {
                executor.notify("That player does not exist.", SYSTEM);
                return;
            }

            PlayerQuestDialog.showPlayerQuests((Player) executor, target);
        } else {
            PlayerQuestDialog.showPlayerQuests((Player) executor);
        }

    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/quests [player]";
    }

    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
    
}
