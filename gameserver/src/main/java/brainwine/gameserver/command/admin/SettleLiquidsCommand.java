package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "settle", description = "Settles all liquids in all active chunks in the current zone. Warning - can cause lag!")
public class SettleLiquidsCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        long now = System.currentTimeMillis();
        Player player = (Player)executor;
        player.notify("Settling liquids...", SYSTEM);
        int updateCount = player.getZone().settleLiquids();
        player.notify(String.format("Liquids were settled! Took %s update(s) in %s millisecond(s)",
                updateCount, System.currentTimeMillis() - now), SYSTEM);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/settle";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
