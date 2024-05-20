package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.prefab.Prefab;

@CommandInfo(name = "prefabs", description = "Displays a list of all prefabs.")
public class PrefabListCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        List<Prefab> prefabs = new ArrayList<>(GameServer.getInstance().getPrefabManager().getPrefabs());
        int pageSize = 8;
        int pageCount = (int)Math.ceil(prefabs.size() / (double)pageSize);
        int page = 1;
        
        if(args.length > 0 && NumberUtils.isDigits(args[0])) {
            page = Math.max(1, Math.min(pageCount, Integer.parseInt(args[0])));
        }
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(page * pageSize, prefabs.size());
        List<Prefab> prefabsToDisplay = prefabs.subList(fromIndex, toIndex);
        executor.notify(String.format("========== Prefab List (Page %s of %s) ==========", page, pageCount), SYSTEM);
        
        for(Prefab prefab : prefabsToDisplay) {
            executor.notify(prefab.getName(), SYSTEM);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/prefabs [page]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
