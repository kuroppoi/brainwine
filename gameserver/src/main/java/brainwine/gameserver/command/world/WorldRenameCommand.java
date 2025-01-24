package brainwine.gameserver.command.world;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

@CommandInfo(name = "wrename", description = "Rename your private world.")
public class WorldRenameCommand extends WorldCommand {
    
    public static final String ACTION_ID = "wrename";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{5,20}$");

    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
            return;
        }

        // Check if command is on cooldown
        if(!player.isGodMode() && zone.isActionOnCooldown(ACTION_ID, 1, ChronoUnit.DAYS)) {
            player.notify("Sorry, you can rename your world only once a day.");
            return;
        }

        ZoneManager zoneManager = GameServer.getInstance().getZoneManager();
        String name = String.join(" ", args).trim().replaceAll(" +", " ");
        
        // Verify name length
        if(name.length() < 5 || name.length() > 20) {
            player.notify("World name must be between 5 and 20 characters.");
            return;
        }
        
        // Verify name pattern
        if(!NAME_PATTERN.matcher(name).matches()) {
            player.notify("World name can only contain letters and numbers.");
            return;
        }
        
        // Check if name already exists
        if(zoneManager.doesZoneExist(name)) {
            player.notify(String.format("World name '%s' is already taken.", name));
            return;
        }
        
        // Try rename zone (shouldn't fail)
        if(!zoneManager.renameZone(zone, name)) {
            player.notify("An unexpected problem occured while renaming your world.", SYSTEM);
            return;
        }

        zone.recordActionTime(ACTION_ID);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wrename <name>";
    }
}
