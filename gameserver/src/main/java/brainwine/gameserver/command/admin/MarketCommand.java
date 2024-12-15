package brainwine.gameserver.command.admin;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneActivity;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "market", description = "Set the market status of the current zone.")
public class MarketCommand extends Command {
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.notify("Market worlds can only be set up by an admin.", NotificationType.SYSTEM);
            return;
        }

        Player player = (Player)executor;

        boolean value = true;

        if(args.length >= 1) {
            try {
                value = Boolean.parseBoolean(args[0]);
            } catch(NumberFormatException e) {
                executor.notify("First argument must be a boolean indicating whether the current zone should be a market.", SYSTEM);
                return;
            }
        }

        Zone zone = player.getZone();

        if(zone == null) {
            executor.notify("Your zone was not found.", NotificationType.SYSTEM);
            return;
        }

        if(!value && !zone.isMarket()) {
            executor.notify("Zone is already not a market."
                    + (zone.getActivity() != ZoneActivity.NONE ? " The zone is a " + zone.getActivity() + "." : "")
                    , NotificationType.SYSTEM);
        } else if(value && zone.isMarket()) {
            executor.notify("Zone is already a market.", NotificationType.SYSTEM);
        } else {
            executor.notify(value
                    ? "Zone is now a market. Trading is possible and further placement of protectors is limited."
                    : "Zone is now not a market. Protectors in the zone will keep their owners."
                    , NotificationType.SYSTEM
            );
        }

        zone.setActivity(value ? ZoneActivity.MARKET : ZoneActivity.NONE);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/market [true|false]";
    }
}
