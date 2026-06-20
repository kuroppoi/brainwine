package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.Skill;

@CommandInfo(name = "skill", description = "View or set a player's skill level.")
public class SkillCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        Skill skill = Skill.fromId(args[1]);
        
        if(skill == null) {
            executor.notify(String.format("Skill must be one of: %s", Arrays.toString(Skill.values()).toLowerCase()), SYSTEM);
            return;
        }
        
        if(args.length == 2) {
            String name = target == executor ? "Your" : target.getName() + "'s";
            executor.notify(String.format("%s %s skill is level %s.", name, skill.getId(), target.getSkillLevel(skill)), SYSTEM);
            return;
        }
        
        int level = 0;
        
        try {
            level = Math.max(1, Math.min(Player.MAX_NATURAL_SKILL_LEVEL, Integer.parseInt(args[2])));
        } catch(NumberFormatException e) {
            executor.notify("Level must be a valid number.", SYSTEM);
            return;
        }
        
        target.setSkillLevel(skill, level);
        target.notify(String.format("Your %s skill has been set to level %s.", skill.getId(), level), SYSTEM);
        executor.notify(String.format("Successfully set %s's %s skill to level %s.", target.getName(), skill.getId(), level), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return String.format("/skill <player> <skill> [level]");
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
