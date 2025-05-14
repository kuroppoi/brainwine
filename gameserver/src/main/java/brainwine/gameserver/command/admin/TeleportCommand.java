package brainwine.gameserver.command.admin;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "teleport", description = "Teleports you or another player to the specified target position or player.", aliases = "tp")
public class TeleportCommand extends Command {
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0 || args.length > 4) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }

        Player player = (Player)executor;
        PlayerManager playerManager = GameServer.getInstance().getPlayerManager();
        ZoneManager zoneManager = GameServer.getInstance().getZoneManager();

        if(args.length == 1) {
            teleportToPlayerOrPlaque(player, player, player.getZone(), args[0]);
        } else if(args.length == 2) {
            try {
                int x = parseXCoordinate(args[0], player.getZone());
                int y = parseYCoordinate(args[1], player.getZone());
                teleportToCoordinates(player, player, player.getZone(), x, y);
            } catch(NumberFormatException e) {
                Player subject = playerManager.getPlayer(args[0]);
                if(subject != null) {
                    teleportToPlayerOrPlaque(player, subject, player.getZone(), args[1]);
                } else {
                    player.notify(String.format("Player '%s' not found.", args[0]));
                }
            }
        } else if(args.length == 3) {
            Zone targetZone = null;
            Player subject = null;
            int x = 0;
            int y = 0;
            boolean done = false;

            if(!done) {
                try {
                    subject = playerManager.getPlayer(args[0]);
                    Objects.requireNonNull(subject);
                    targetZone = player.getZone();
                    Objects.requireNonNull(targetZone);
                    x = parseXCoordinate(args[1], targetZone);
                    y = parseYCoordinate(args[2], targetZone);
                    done = true;
                } catch (Exception e){
                }

                if(done) {
                    teleportToCoordinates(player, subject, targetZone, x, y);
                    return;
                }
            }

            if(!done) {
                try {
                    targetZone = zoneManager.getZoneByName(args[0]);
                    Objects.requireNonNull(targetZone);
                    x = parseXCoordinate(args[1], targetZone);
                    y = parseYCoordinate(args[2], targetZone);
                    subject = player;
                    done = true;
                } catch (Exception e){
                }

                if(done) {
                    teleportToCoordinates(player, subject, targetZone, x, y);
                    return;
                }
            }

            if(!done) {
                try {
                    subject = playerManager.getPlayer(args[0]);
                    Objects.requireNonNull(subject);
                    targetZone = zoneManager.getZoneByName(args[1]);
                    Objects.requireNonNull(targetZone);
                    done = true;
                } catch(Exception e) {
                }

                if(done) {
                    teleportToPlayerOrPlaque(player, subject, targetZone, args[2]);
                    return;
                }
            }

            player.notify("Wrong arguments given.");
        } else {
            try {
                Player subject = playerManager.getPlayer(args[0]);
                Objects.requireNonNull(subject);
                Zone targetZone = zoneManager.getZoneByName(args[1]);
                Objects.requireNonNull(targetZone);
                int x = parseXCoordinate(args[2], targetZone);
                int y = parseYCoordinate(args[3], targetZone);

                teleportToCoordinates(player, subject, targetZone, x, y);
            } catch(Exception e) {
                player.notify("Wrong arguments given.");
            }
        }
    }

    private boolean checkSubjectAndZone(Player player, Player subject, Zone targetZone) {
        if(targetZone == null) {
            player.notify("Sorry, the target world is null.");
            return false;
        }

        if(!player.isAdmin()) {
            if(!targetZone.hasMassTeleporter()) {
                player.notify("No mass teleportation machine is operational in this world.");
                return false;
            }

            if(player != subject) {
                player.notify("Only admins can teleport other players.");
                return false;
            }

            if(subject.getZone() != targetZone || player.getZone() != targetZone) {
                player.notify("Sorry, only admins can teleport players out of and across worlds.");
                return false;
            }
        }

        return true;
    }

    private void teleportToPlayerOrPlaque(Player player, Player subject, Zone targetZone, String name) {
        if(!checkSubjectAndZone(player, subject, targetZone)) return;

        Player target = GameServer.getInstance().getPlayerManager().getPlayer(name);
        if(target != null) {
            if(!targetZone.getMassTeleporterConfiguration().getTeleportToPlayerAccess().isPrivileged(player, targetZone)) {
                player.notify("You are not allowed to teleport to players in the target world.");
                return;
            }

            if(!target.isOnline()) {
                player.notify(String.format("Player '%s' is not online.", target.getName()));
                return;
            }

            if(subject.getZone() != target.getZone() && !targetZone.getMassTeleporterConfiguration().getSummonOtherPlayerAccess().isPrivileged(player, targetZone)) {
                player.notify("You are not allowed to summon other players in this world.");
                return;
            }

            if(subject == target) {
                player.notify("You cannot teleport a player to themselves.");
                return;
            }

            doTeleport(player, subject, target.getZone(), (int)target.getX(), (int)target.getY());
            return;
        }

        Vector2i targetPosition = this.getLandmarkPosition(targetZone, name);
        if(targetPosition != null) {
            if(!targetZone.getMassTeleporterConfiguration().getTeleportToPlaqueAccess().isPrivileged(player, targetZone)) {
                player.notify("You are not allowed to teleport to plaques in this world.");
                return;
            }
            doTeleport(player, subject, targetZone, targetPosition.getX(), targetPosition.getY());
            return;
        }

        player.notify(String.format("Player or landmark '%s' not found.", name));
    }

    private void teleportToCoordinates(Player player, Player subject, Zone targetZone, int x, int y) {
        if(!player.isAdmin()) {
            player.notify("Only admins are allowed to teleport to exact coordinates.");
            return;
        }

        if(!checkSubjectAndZone(player, subject, targetZone)) return;

        doTeleport(player, subject, targetZone, x, y);
    }

    private void doTeleport(Player player, Player subject, Zone targetZone, int x, int y) {
        if(!subject.isOnline()) {
            player.notify(String.format("Player '%s' is not online.", subject.getName()));
            return;
        }

        if(!player.isAdmin()) {
            if(!targetZone.getMassTeleporterConfiguration().isEnabled()) {
                player.notify(String.format("There is no mass teleporter enabled in %s.", targetZone.getName()));
                return;
            }

            if(!targetZone.isAreaExplored(x, y)) {
                player.notify("That area hasn't been explored yet.");
                return;
            }

            if(targetZone.isChunkLoaded(x, y) && (targetZone.isBlockSolid(x, y) || targetZone.isBlockSolid(x, y - 1))) {
                player.notify("Teleportation destination is obstructed.");
                return;
            }

            // We don't consider single blocks to be protected against teleportation.
            if(!targetZone.getMassTeleporterConfiguration().getTeleportInProtectedAreaAccess().isPrivileged(player, targetZone) && targetZone.isBlockProtected(x, y, player, true)) {
                player.notify("Sorry, you can't teleport to areas protected against you in this world.");
                return;
            }
        }

        // Check if coordinates are in bounds
        if(!targetZone.areCoordinatesInBounds(x, y)) {
            player.notify("Cannot teleport out of bounds!", SYSTEM);
            return;
        }

        final Runnable task = () -> {
            if(targetZone == subject.getZone()) {
                subject.teleport(x, y);
            } else {
                targetZone.giveTemporaryAccess(subject);
                subject.changeZone(targetZone, x, y);
            }
        };

        if(player.isGodMode() || player.equals(subject)) {
            task.run();
        } else {
            if(subject.getZone().isActionOnCooldown("failed teleport request", 10, ChronoUnit.SECONDS)) {
                player.notify("Sorry, further teleport requests have been blocked for 10 seconds.", SYSTEM);
                return;
            }

            double distance = MathUtils.distance(x, y, player.getX(), player.getY());
            subject.showDialog(
                    new Dialog()
                            .setTitle("Teleport Request")
                            .addSection(new DialogSection().setText(
                                    player.getName() +
                                    " wants to teleport you to " +
                                    targetZone.getReadableCoordinates(x, y) +
                                    (distance <= 5.0 ? " (near themselves)" : "") +
                                    (targetZone == player.getZone()
                                            ? "."
                                            : " in " + targetZone.getName() + ".") +
                                    " Click OK to accept."
                            )),
                    ans -> {
                        if(ans.length >= 1 && "cancel".equals(ans[0])) {
                            player.notify(subject.getName() + " has dismissed your teleport request.", SYSTEM);
                            subject.getZone().recordActionTime("failed teleport request");
                        } else {
                            task.run();
                        }
                    });
            player.notify("Your teleport request has been sent to " + subject.getName(), SYSTEM);
        }
    }

    private int parseXCoordinate(String value, Zone targetZone) throws NumberFormatException {
        return parseNumberWithDirection(value, targetZone.getWidth() / 2, new String[] { "left", "west", "l", "w" }, new String[] { "right", "east", "r", "e" } );
    }

    private int parseYCoordinate(String value, Zone targetZone) throws NumberFormatException {
        return parseNumberWithDirection(value, targetZone.getGroundHeight(), new String[] { "above", "up", "a", "u" }, new String[] { "below", "down", "b", "d" } );
    }

    private int parseNumberWithDirection(String value, int offset, String[] lowerDirection, String[] upperDirection) throws NumberFormatException {
        int direction = 0;
        int unitLength = 0;

        for(String unit : lowerDirection) {
            if(value.endsWith(unit)) {
                direction = -1;
                unitLength = unit.length();
                break;
            }
        }

        for(String unit : upperDirection) {
            if(value.endsWith(unit)) {
                direction = 1;
                unitLength = unit.length();
                break;
            }
        }

        if(unitLength == 0) {
            return Integer.parseInt(value);
        } else {
            return offset + direction * Integer.parseInt(value.substring(0, value.length() - unitLength));
        }
    }

    private Vector2i getLandmarkPosition(Zone zone, String name) {
        String landmarkName = name.toLowerCase();

        int x = -1;
        int y = -1;
        boolean found = false;
        for(MetaBlock metaBlock : zone.getMetaBlocksWithUse(ItemUseType.LANDMARK)) {
            boolean thisIsIt = false;

            if(landmarkName.equalsIgnoreCase(metaBlock.getStringProperty("n"))) {
                thisIsIt = true;
            }

            if(thisIsIt) {
                x = metaBlock.getX();
                y = metaBlock.getY();
                found = true;
                break;
            }
        }

        return found ? new Vector2i(x, y) : null;
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/tp [target description]";
    }

    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }

    @Override
    public boolean useSmartArguments() {
        return true;
    }
}
