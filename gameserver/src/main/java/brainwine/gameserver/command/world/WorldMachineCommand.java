package brainwine.gameserver.command.world;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.WorldMachineConfiguration;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wmachine", description = "Bring up the world machine configuration menu.")
public class WorldMachineCommand extends Command {
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) return;
        Player player = (Player)executor;
        Zone zone = player.getZone();
        if(zone == null) return;

        if(!checkArgumentCount(player, args, 1)) {
            return;
        }

        WorldMachineConfiguration machine = null;
        switch(args[0]) {
            case "spawn":
            case "spawner":
                machine = zone.getMassSpawnerConfiguration();
                break;
            case "teleport":
            case "teleporter":
                machine = zone.getMassTeleporterConfiguration();
                break;
            case "weather":
                machine = zone.getWeatherMachineConfiguration();
                break;
            case "holo":
            case "holograph":
                machine = zone.getHolographConfiguration();
                break;
            default:
                player.notify("World machine configuration for use " + args[0] + " not found.", NotificationType.SYSTEM);
                return;
        }

        machine.configure(player, zone, Float.POSITIVE_INFINITY, -1, -1);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wmachine [type]";
    }

    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
