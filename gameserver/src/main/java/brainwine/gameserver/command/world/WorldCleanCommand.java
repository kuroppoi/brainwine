package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            Dialog dialog = DialogHelper.messageDialog(
                    "Clean-Up Confirmation",
                    "This action will clean up all unprotected dirt, sandstone, "
                            + "and limestone without a backdrop in your zone. Are you sure you would like to proceed?"
            );
            for(DialogSection section : getForm()) {
                dialog.addSection(section);
            }
            player.showDialog(dialog, ans -> followUpJunk(player, zone, ans));
        }

        if("all".equals(args[0])) {
            Dialog dialog = DialogHelper.messageDialog(
                    "Clean-Up Confirmation",
                    "This action will remove all blocks "
                            + "except those in the bedrock layer. Are you sure you would like to proceed?"
            );
            for(DialogSection section : getForm()) {
                dialog.addSection(section);
            }
            player.showDialog(dialog, ans -> followUpAll(player, zone, ans));
        }
    }

    private List<DialogSection> getForm() {
        List<DialogSection> result = new ArrayList<>();

        result.add(new DialogSection().setText("Should these blocks remain in player-protected areas? ").setInput(new DialogSelectInput().setOptions("Yes", "No").setKey("player-protected-areas")));
        result.add(new DialogSection().setText("Should these blocks remain in naturally-protected areas? ").setInput(new DialogSelectInput().setOptions("Yes", "No").setKey("naturally-protected-areas")));

        return result;
    }

    private boolean shouldKeepPlayerProtectedAreas(Object[] ans) {
        return "Yes".equals(ans[0]);
    }

    private boolean shouldKeepNaturallyProtectedAreas(Object[] ans) {
        return "Yes".equals(ans[1]);
    }

    private List<MetaBlock> getProtectors(Zone zone, Object[] ans) {
        final boolean playerProtectedAreas = shouldKeepPlayerProtectedAreas(ans);
        final boolean naturallyProtectedAreas = shouldKeepNaturallyProtectedAreas(ans);
        return zone.getMetaBlocks(m ->
                playerProtectedAreas && m.getOwner() != null ||
                naturallyProtectedAreas && m.getOwner() == null
        );
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
                final List<MetaBlock> protectors = getProtectors(zone, ans);
                transformBlocks(zone, (x, y) -> {
                    Block block = zone.getBlock(x, y);
                    if(block.getBaseItem().isAir() && block.getBackItem().isAir()) {
                        int code = block.getFrontItem().getCode();
                        if(code == 510 || code == 511 || code == 512) {
                            if(!zone.isBlockProtected(x, y, null, protectors)) {
                                zone.updateBlock(x, y, Layer.FRONT, Item.AIR);
                            }
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

    // followUpAll has a lot more mitigation than followUpJunk even if the implementations look similar,
    // so be mindful when refactoring.
    private void followUpAll(Player player, Zone zone, Object[] ans) {
        if(cancelled(ans)) {
            return;
        }
        zone.freeze();

        new Thread(() -> {
            try {
                final List<MetaBlock> protectors = getProtectors(zone, ans);
                final boolean naturallyProtectedAreas = shouldKeepNaturallyProtectedAreas(ans);
                transformBlocks(zone, (x, y) -> {
                    // Do not delete bedrock layers.
                    if(y < zone.getHeight() - 1 && (zone.getBiome() != Biome.DEEP || y > 0)) {
                        Item frontItem = zone.getBlock(x, y).getFrontItem();
                        MetaBlock metaBlock = zone.getMetaBlock(x, y);

                        // We need to delete natural protectors to prevent players from raiding dug out dungeons.
                        if(
                                (
                                    !naturallyProtectedAreas
                                            && !frontItem.hasUse(ItemUseType.ZONE_TELEPORT)
                                            && frontItem.hasField()
                                            && metaBlock != null
                                            && !metaBlock.hasOwner()
                                )
                                || !zone.isBlockProtected(x, y, null, protectors)
                        ) {
                            zone.updateBlock(x, y, Layer.BASE, Item.AIR);
                            zone.updateBlock(x, y, Layer.BACK, Item.AIR);
                            zone.updateBlock(x, y, Layer.FRONT, Item.AIR);
                            zone.updateBlock(x, y, Layer.LIQUID, Item.AIR);
                        }
                    }
                });
                Arrays.fill(zone.getChunksExplored(), true);
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
