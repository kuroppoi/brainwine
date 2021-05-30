package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.prefab.PrefabManager;
import brainwine.gameserver.zone.Zone;

public class ExportCommand extends Command {
    
    public static final int SIZE_LIMIT = 10000;
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {    
        if(args.length < 5) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Zone zone = ((Player)executor).getZone();
        PrefabManager prefabManager = GameServer.getInstance().getPrefabManager();
        String name = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        
        if(prefabManager.getPrefab(name) != null) {
            executor.notify("A prefab with that name already exists.", ALERT);
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
            executor.notify("Parameters must be valid numbers.", ALERT);
            return;
        }
        
        if(width < 0 || height < 0) {
            executor.notify("Width and height must be positive.", ALERT);
            return;
        } else if(width * height > SIZE_LIMIT) {
            executor.notify(String.format("Sorry, your prefab is too large. Max size: %s blocks.", SIZE_LIMIT), ALERT);
            return;
        } else if(x < 0 || x + width >= zone.getWidth() || y < 0 || y + height >= zone.getHeight()) {
            executor.notify("These coordinates are out of bounds.", ALERT);
            return;
        }
        
        Prefab prefab = zone.chop(x, y, width, height);
        
        if(prefab == null) {
            executor.notify("Sorry, something went wrong. Please try again.", ALERT);
            return;
        }
        
        executor.notify("Exporting your prefab ...", ALERT);
        
        try {
            prefabManager.registerPrefab(name, prefab);
            executor.notify(String.format("Your prefab '%s' was successfully exported!", name), ALERT);
        } catch (Exception e) {
            executor.notify(String.format("An error occured while exporting prefab '%s': %s", name, e.getMessage()), ALERT);
        }
    }
    
    @Override
    public String getName() {
        return "export";
    }
    
    @Override
    public String getDescription() {
        return "Exports a section of a zone to a prefab file.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/export <x> <y> <width> <height> <name>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
