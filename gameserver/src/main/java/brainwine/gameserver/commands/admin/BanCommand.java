package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.util.DateTimeUtils;

@CommandInfo(name = "ban", description = "Bans a player from the server.")
public class BanCommand extends Command {

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
        
        if(target == executor) {
            executor.notify("You cannot ban yourself.", SYSTEM);
            return;
        }
        
        if(target.isBanned()) {
            executor.notify(String.format("%s is currently already banned.", target.getName()), SYSTEM);
            return;
        }
        
        int duration = DateTimeUtils.parseFormattedDuration(args[1]);
        
        if(duration < 0) {
            executor.notify("Invalid duration. Example: '10d2h30m' would be 10 days, 2 hours and 30 minutes.", SYSTEM);
            executor.notify("Time units: y = years, w = weeks, d = days, h = hours, m = minutes", SYSTEM);
            return;
        }
        
        String reason = "The ban hammer has spoken!";
        
        if(args.length > 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }
        
        OffsetDateTime endDate = OffsetDateTime.now().plusMinutes(duration);
        target.ban(executor instanceof Player ? (Player)executor : null, reason, endDate);
        executor.notify(String.format("Banned %s until %s for '%s'", 
                target.getName(), endDate.format(DateTimeFormatter.RFC_1123_DATE_TIME), reason), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/ban <player> <duration> [reason]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
