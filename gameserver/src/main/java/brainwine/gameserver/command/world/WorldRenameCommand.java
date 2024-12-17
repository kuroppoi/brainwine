package brainwine.gameserver.command.world;

import java.util.regex.Pattern;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

@CommandInfo(name = "wrename", description = "Rename your private world.")
public class WorldRenameCommand extends WorldCommand {
    
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{5,20}$");

    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
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
        
        zoneManager.renameZone(zone, name);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wrename <name>";
    }
}
