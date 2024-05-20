package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import java.util.regex.Pattern;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.prefab.PrefabManager;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "export", description = "Exports a section of a zone to a prefab file.")
public class ExportCommand extends Command {
    
    public static final Pattern PREFAB_NAME_PATTERN = Pattern.compile("\\w+(/\\w+)*");
    public static final int SIZE_LIMIT = 10000;
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {    
        if(args.length < 5) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        boolean overwrite = args.length >= 6 && args[5].equalsIgnoreCase("overwrite");
        Zone zone = ((Player)executor).getZone();
        PrefabManager prefabManager = GameServer.getInstance().getPrefabManager();
        String name = args[4].toLowerCase();
        
        if(!overwrite && prefabManager.prefabExists(name)) {
            executor.notify("A prefab with that name already exists. Add 'overwrite' at the end of the command if you wish to overwrite it.", SYSTEM);
            return;
        } else if(!PREFAB_NAME_PATTERN.matcher(name).matches()) {
            executor.notify("Please enter a valid prefab name. Example: dungeons/my_epic_dungeon (or just my_epic_dungeon)", SYSTEM);
            return;
        }
        
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            width = Integer.parseInt(args[2]);
            height = Integer.parseInt(args[3]);
        } catch(NumberFormatException e) {
            executor.notify("Parameters must be valid numbers.", SYSTEM);
            return;
        }
        
        if(width < 0 || height < 0) {
            executor.notify("Width and height must be positive.", SYSTEM);
            return;
        } else if(width * height > SIZE_LIMIT) {
            executor.notify(String.format("Sorry, your prefab is too large. Max size: %s blocks.", SIZE_LIMIT), SYSTEM);
            return;
        } else if(x < 0 || x + width >= zone.getWidth() || y < 0 || y + height >= zone.getHeight()) {
            executor.notify("These coordinates are out of bounds.", SYSTEM);
            return;
        }
        
        Prefab prefab = zone.createPrefabFromSection(name, x, y, width, height);
        
        if(prefab == null) {
            executor.notify("Sorry, something went wrong. Please try again.", SYSTEM);
            return;
        }
        
        executor.notify(String.format("Exporting your prefab as '%s' ...", name), SYSTEM);
        
        try {
            prefabManager.createPrefab(prefab, overwrite);
            executor.notify(String.format("Your prefab '%s' was successfully exported!", name), SYSTEM);
        } catch (Exception e) {
            executor.notify(String.format("An error occured while exporting prefab '%s': %s", name, e.getMessage()), SYSTEM);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/export <x> <y> <width> <height> <name> [overwrite]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
