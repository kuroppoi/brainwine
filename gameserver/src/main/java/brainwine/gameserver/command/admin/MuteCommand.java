package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.DateTimeUtils;

@CommandInfo(name = "mute", description = "Mutes a player, preventing them from chatting.", aliases = "silence")
public class MuteCommand extends Command {

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
            executor.notify("You cannot mute yourself.", SYSTEM);
            return;
        }
        
        if(target.isMuted()) {
            executor.notify(String.format("%s is currently already muted.", target.getName()), SYSTEM);
            return;
        }
        
        int duration = DateTimeUtils.parseFormattedDuration(args[1]);
        
        if(duration < 0) {
            executor.notify("Invalid duration. Example: '10d2h30m' would be 10 days, 2 hours and 30 minutes.", SYSTEM);
            executor.notify("Time units: y = years, w = weeks, d = days, h = hours, m = minutes", SYSTEM);
            return;
        }
        
        String reason = "You have been muted.";
        
        if(args.length > 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }
        
        OffsetDateTime endDate = OffsetDateTime.now().plusMinutes(duration);
        target.mute(executor instanceof Player ? (Player)executor : null, reason, endDate);
        executor.notify(String.format("Muted %s until %s for '%s'", 
                target.getName(), endDate.format(DateTimeFormatter.RFC_1123_DATE_TIME), reason), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/mute <player> <duration> [reason]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}