package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.loot.LootManager;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;

@CommandInfo(name = "loot", description = "Awards loot to a player.")
public class LootCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        // Check if player exists
        if(target == null) {
            executor.notify("That player does not exist.", SYSTEM);
            return;
        }
        
        // Check if player is online
        if(!target.isOnline()) {
            executor.notify(String.format("%s is not online.", target.getName()), SYSTEM);
            return;
        }
        
        int luck = target.getTotalSkillLevel(Skill.LUCK);
        int maxLuck = (int)(LootManager.MAX_BONUS_ROLLS * LootManager.LEVELS_PER_BONUS_ROLL);
        
        if(args.length >= 3) {
            try {
                luck = Math.max(1, Math.min(maxLuck, Integer.parseInt(args[2])));
            } catch(NumberFormatException e) {
                executor.notify("Luck must be a valid number.", SYSTEM);
                return;
            }
        }
        
        String category = args[1];
        LootManager lootManager = GameServer.getInstance().getLootManager();
        
        // Check if loot table exists
        if(lootManager.getLootTable(category) == null) {
            executor.notify(String.format("Loot category must be one of: %s", lootManager.getLootCategories()), SYSTEM);
            return;
        }
        
        Loot loot = lootManager.getRandomLoot(luck, target.getZone().getBiome(), target.getInventory().getWardrobe(), category);
        
        // Check if eligible loot was found
        if(loot == null) {
            executor.notify(String.format("Could not find any eligible loot for category '%s'.", category), SYSTEM);
            return;
        }
        
        target.awardLoot(loot);
        executor.notify(String.format("Awarded level %s %s loot to %s.", luck, category, target.getName()), SYSTEM);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/loot <player> <category> [luck]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
