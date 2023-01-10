package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

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
    public String getName() {
        return "settleliquids";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] {"settle"};
    }
    
    @Override
    public String getDescription() {
        return "Settles all liquids in all active chunks in the current zone. Warning - can cause lag!";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
