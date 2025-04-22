package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.ChatType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.ChatMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "tell", description = "Send a private message to a fellow player.", aliases = "t")
public class TellCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }

        Player executorPlayer = executor instanceof Player ? (Player) executor : null;
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        if(target == null) {
            executor.notify("That player does not exist.", SYSTEM);
            return;
        }

        if(!target.isOnline()) {
            executor.notify("Player is not online.", SYSTEM);
            return;
        }

        if(executorPlayer != null && !executorPlayer.isGodMode() && target.getZone() != executorPlayer.getZone()) {
            executor.notify("Player is not in the same world.", SYSTEM);
            return;
        }

        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        String executorName = executorPlayer != null ? executorPlayer.getName() : "Server";
        int executorId = executorPlayer != null ? executorPlayer.getId() : 0;

        if(target.isV3()) {
            String targetMessage = String.format("<color=#012398>%s whispers: %s</color>", executorName, message);
            target.sendMessage(new ChatMessage(0, targetMessage, ChatType.PRIVATE));
        } else {
            target.sendMessage(new ChatMessage(executorId, message, ChatType.PRIVATE));
        }

        executor.notify(String.format("You whispered to %s: %s", target.getName(), message), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/tell <player> <message>";
    }
}
