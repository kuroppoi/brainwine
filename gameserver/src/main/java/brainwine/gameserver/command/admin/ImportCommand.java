package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.prefab.Prefab;

@CommandInfo(name = "import", description = "Places a prefab at the specified location.")
public class ImportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 1) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
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
                player.notify("X and Y must be valid numbers.", SYSTEM);
                return;
            }
        }
        
        String name = args[0];
        Prefab prefab = GameServer.getInstance().getPrefabManager().getPrefab(name);
        
        if(prefab == null) {
            player.notify(String.format("Prefab '%s' does not exist. Type '/prefabs' for a list of prefabs.", name), SYSTEM);
            return;
        }
        
        player.getZone().placePrefab(prefab, x, y);
        player.notify(String.format("Successfully imported '%s' @ [x: %s, y: %s, width: %s, height: %s]", 
                name, x, y, prefab.getWidth(), prefab.getHeight()), SYSTEM);
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
