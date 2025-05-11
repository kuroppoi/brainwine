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
        int difficulty = zone.getMassSpawnerConfiguration().getDifficulty();
        if(args.length >= 2) {
            try {
                difficulty = Integer.parseInt(args[1]);
            } catch(Exception e) {
                executor.notify("Invalid difficulty.", SYSTEM);
                return;
            }
        }

        if(!executor.isAdmin() && !zone.getMassSpawnerConfiguration().isEnabled()) {
            executor.notify("No mass spawner is enabled in this world.", NotificationType.POPUP);
            return;
        }

        if(!isPrivileged(executor, zone, zone.getMassSpawnerConfiguration().getEvokeAccess())) {
            executor.notify("You are not allowed to evoke players in this world.", NotificationType.POPUP);
            return;
        }

        if(System.currentTimeMillis() < zone.getEntityManager().getLastInvasionAt() + 3000) {
            executor.notify("You must wait 3 seconds between invasions.", NotificationType.POPUP);
            return;
        }

        zone.getEntityManager().startInvasion(target, difficulty);
        executor.notify("Commencing evocation!", NotificationType.POPUP);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/evoke <player> <difficulty>";
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
