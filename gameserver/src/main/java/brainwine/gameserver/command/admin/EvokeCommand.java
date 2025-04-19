package brainwine.gameserver.command.admin;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "evoke", description = "Makes brains or revenants invade the personal space of the player!")
public class EvokeCommand extends Command {
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }

        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);

        if(target == null) {
            executor.notify("This player does not exist.", NotificationType.POPUP);
            return;
        }

        if(!target.isOnline()) {
            executor.notify(String.format("Player '%s' is not online.", target.getName()), NotificationType.POPUP);
            return;
        }

        Zone zone = target.getZone();

        if(!isPrivileged(executor, zone, zone.getMassSpawnerConfiguration().getEvokeAccess())) {
            executor.notify("You are not allowed to evoke players in this world.", NotificationType.POPUP);
            return;
        }

        if(System.currentTimeMillis() < zone.getEntityManager().getLastInvasionAt() + 300000) {
            executor.notify("You must wait 5 minutes between invasions.", NotificationType.POPUP);
            return;
        }

        zone.getEntityManager().startInvasion(target);
        executor.notify("Commencing evocation!", NotificationType.POPUP);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/evoke <player>";
    }

    private boolean isPrivileged(CommandExecutor executor, Zone zone, CommandAccessLevel needed) {
        if(executor == null || zone == null || needed == null) return false;
        if(executor instanceof GameServer) return true;
        if(executor.isAdmin()) return true;

        Player player = (Player)executor;
        if(needed == CommandAccessLevel.OWNERS) return zone.isOwner(player);
        if(needed == CommandAccessLevel.MEMBERS) return zone.isOwner(player) || zone.isMember(player);

        return true;

    }
}
