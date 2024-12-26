package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;

@CommandInfo(name = "wclean", description = "Clean the current world from blocks.")
public class WorldCleanCommand extends WorldCommand {
    private static final Logger logger = LogManager.getLogger();
    public static final String ACTION_ID = "wclean";

    @Override
    public void execute(final Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
            return;
        }

        if(!player.isGodMode() && zone.isActionOnCooldown(ACTION_ID, 1, ChronoUnit.DAYS)) {
            player.notify("Sorry, you can clean your world only once a day.");
            return;
        }

        if("junk".equals(args[0])) {
            player.showDialog(DialogHelper.messageDialog(
                    "Clean-Up Confirmation",
                    "This action will clean up all unprotected dirt, sandstone, "
                            + "and limestone without a backdrop in your zone. Are you sure you would like to proceed?"
            ), ans -> followUpJunk(player, zone, ans));
        }

        if("all".equals(args[0])) {
            player.showDialog(DialogHelper.messageDialog(
                    "Clean-Up Confirmation",
                    "This action will remove all blocks "
                            + "except those in the bedrock layer. Are you sure you would like to proceed?"
            ), ans -> followUpAll(player, zone, ans));
        }

        zone.recordActionTime(ACTION_ID);
    }

    private boolean cancelled(Object[] ans) {
        return ans.length >= 1 && "cancel".equals(ans[0]);
    }

    private void followUpJunk(Player player, Zone zone, Object[] ans) {
        if(cancelled(ans)) {
            return;
        }
        zone.freeze();

        new Thread(() -> {
            try {
                transformBlocks(zone, (x, y) -> {
                    Block block = zone.getBlock(x, y);
                    if(block.getBaseItem().isAir() && block.getBackItem().isAir()) {
                        int code = block.getFrontItem().getCode();
                        if(code == 510 || code == 511 || code == 512) {
                            zone.updateBlock(x, y, Layer.FRONT, Item.AIR);
                        }
                    }
                });
                player.notify("Zone " + zone.getName() + " is now cleaned up.", NotificationType.SYSTEM);
            } catch (Exception e) {
                logger.error(e);
                player.notify("There has been an error during cleanup of zone " + zone.getName(), NotificationType.SYSTEM);
            } finally {
                zone.thaw();
                zone.recordActionTime(ACTION_ID);
            }
        }).start();
    }

    private void followUpAll(Player player, Zone zone, Object[] ans) {
        if(cancelled(ans)) {
            return;
        }
        zone.freeze();

        new Thread(() -> {
            try {
                transformBlocks(zone, (x, y) -> {
                    if (y < zone.getHeight() - 1) {
                        zone.updateBlock(x, y, Layer.BASE, Item.AIR);
                        zone.updateBlock(x, y, Layer.BACK, Item.AIR);
                        zone.updateBlock(x, y, Layer.FRONT, Item.AIR);
                        zone.updateBlock(x, y, Layer.LIQUID, Item.AIR);
                    }
                });
                player.notify("Zone " + zone.getName() + " is now cleaned up.", NotificationType.SYSTEM);
            } catch(Exception e) {
                logger.error(e);
                player.notify("There has been an error during cleanup of zone " + zone.getName(), NotificationType.SYSTEM);
            } finally {
                zone.thaw();
                zone.recordActionTime(ACTION_ID);
            }
        }).start();
    }

    private void transformBlocks(Zone zone, BiConsumer<Integer, Integer> transformer) {
        int width = zone.getWidth();
        int height = zone.getHeight();
        for(int ci = 0; ci < width; ci += zone.getChunkWidth()) {
            for(int cj = 0; cj < height; cj += zone.getChunkHeight()) {
                for(int i = ci; i < ci + zone.getChunkWidth(); i++) {
                    for (int j = cj; j < cj + zone.getChunkHeight(); j++) {
                        transformer.accept(i, j);
                    }
                }
            }
        }
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wclean [junk|all]";
    }
}
