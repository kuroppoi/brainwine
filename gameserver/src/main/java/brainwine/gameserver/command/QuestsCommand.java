package brainwine.gameserver.command;

import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.quest.PlayerQuestDialog;

@CommandInfo(name = "quests", description = "Lists your ongoing and completed quests.")
public class QuestsCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if (!(executor instanceof Player)) {
            executor.notify("You can only view your quests as a player!", NotificationType.SYSTEM);
        }
        
        PlayerQuestDialog.showPlayerQuests((Player) executor);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/quests";
    }

    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
    
}
