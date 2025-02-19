package brainwine.gameserver.item.interactions;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Placement;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public class WorldMachineInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        Object itemUse = item.getUse(ItemUseType.WORLD_MACHINE);
        if(!(entity instanceof Player) || !(itemUse instanceof String)) return;

        Player player = (Player) entity;

        if(!canInteract(player, zone)) return;

        switch ((String) itemUse) {
            case "spawner":
                player.showDialog(DialogHelper.getDialog("world_machines.spawner.menu"), ans -> {
                    if(ans.length == 0 || !(ans[0] instanceof String)) return;

                    switch ((String) ans[0]) {
                        case "configure":
                            player.showDialog(zone.getMassSpawnerConfiguration().getConfigurationDialog(), conf -> configureSpawner(player, zone, conf));
                            break;
                        case "move":
                            move(player, zone, metaBlock);
                            break;
                        case "dismantle":
                            dismantle(player, zone, x, y);
                            break;
                    }
                });
                break;
            case "teleport":
                player.showDialog(DialogHelper.getDialog("world_machines.teleport.menu"), ans -> {
                    if(ans.length == 0 || !(ans[0] instanceof String)) return;

                    switch ((String) ans[0]) {
                        case "configure":
                            player.showDialog(zone.getMassTeleporterConfiguration().getConfigurationDialog(), conf -> configureTeleport(player, zone, conf));
                            break;
                        case "deactivate_natural_teleporters":
                            deactivateNaturalTeleporters(player, zone);
                            break;
                        case "move":
                            move(player, zone, metaBlock);
                            break;
                        case "dismantle":
                            dismantle(player, zone, x, y);
                            break;
                    }
                });
                break;
        }
    }

    public boolean canInteract(Player player, Zone zone) {
        if(player.isGodMode() || zone.isOwner(player)) {
            return true;
        } else {
            player.notify("Sorry, you do not own this world.");
            return false;
        }
    }

    public void deactivateNaturalTeleporters(Player player, Zone zone) {
        if(canInteract(player, zone)) {
            for (MetaBlock metaBlock : zone.getMetaBlocksWithUse(ItemUseType.TELEPORT)) {
                if(!metaBlock.hasOwner() && !metaBlock.getItem().hasUse(ItemUseType.ZONE_TELEPORT)) {
                    zone.updateBlock(metaBlock.getX(), metaBlock.getY(), Layer.FRONT, Item.AIR);
                }
            }

            player.notify("Natural teleporters destroyed!");
        }
    }

    public void configureSpawner(Player player, Zone zone, Object[] ans) {
        if(canInteract(player, zone)) {
            zone.getMassSpawnerConfiguration().configureFromDialog(player, ans);
        }
    }

    public void configureTeleport(Player player, Zone zone, Object[] ans) {
        if(canInteract(player, zone)) {
            zone.getMassTeleporterConfiguration().configureFromDialog(player, ans);
        }
    }

    public void move(Player player, Zone zone, MetaBlock metaBlock) {
        player.setTransmittableBlock(metaBlock);
        player.notify("Place a beacon to move the machine. The machine's lower left corner will replace the beacon.");
    }

    public void dismantle(Player player, Zone zone, int x, int y) {
        if(canInteract(player, zone)) {
            player.notify("World machines can not yet be dismantled, but this feature is coming soon!");
        }
    }
}
