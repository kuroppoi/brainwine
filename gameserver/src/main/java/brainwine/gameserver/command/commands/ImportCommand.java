package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;
import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.prefab.Prefab;

public class ImportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        int x = 0;
        int y = 0;
        Player player = (Player)executor;
        
        if(args.length < 3) {
            x = (int)player.getX();
            y = (int)player.getY() + 1;
        } else {
            try {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                player.notify("X and Y must be valid numbers.", ALERT);
                return;
            }
        }
        
        String name = args[0];
        Prefab prefab = GameServer.getInstance().getPrefabManager().getPrefab(name);
        
        if(prefab == null) {
            player.notify("Sorry, could not find a prefab with that name.", ALERT);
            return;
        }
        
        player.getZone().placePrefab(prefab, x, y);
        player.notify(String.format("Successfully imported '%s' @ [x: %s, y: %s, width: %s, height: %s]", 
                name, x, y, prefab.getWidth(), prefab.getHeight()), SYSTEM);
    }

    @Override
    public String getName() {
        return "import";
    }
    
    @Override
    public String getDescription() {
        return "Places a prefab at your or a specified location.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/import <prefab> [<x> <y>]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
