package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

@CommandInfo(name = "wrule", description = "Set world mechanics rules.")
public class WorldRuleCommand extends WorldCommand {
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 0, 1, 2)) {
            return;
        }

        if(args.length == 2) {
            String message = zone.getRules().setRule(player, args[0], args[1]);

            player.notify(message == null ? "Successfully set rule" : message, SYSTEM);
        } else {
            List<String> rules = zone.getRules().getRules(player);
            int pageSize = 10;
            int pageCount = (int)Math.ceil((double)rules.size() / pageSize);

            int page = 1;

            if(args.length == 1) {
                if(NumberUtils.isDigits(args[0])) {
                    page = Math.max(1, Math.min(pageCount, Integer.parseInt(args[0])));
                } else {
                    player.notify(zone.getRules().getRule(player, args[0]), SYSTEM);
                    return;
                }
            }

            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(page * pageSize, rules.size());
            List<String> rulesToDisplay = rules.subList(fromIndex, toIndex);
            player.notify(String.format("========== Command List (Page %s of %s) ==========", page, pageCount), SYSTEM);

            for(String rule : rulesToDisplay) {
                player.notify("/wrule " + rule, SYSTEM);
            }
        }
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wrule [key] [value]";
    }
}
