package brainwine.gameserver.item.interactions;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public class WorldMachineInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        Object itemUse = item.getUse(ItemUseType.WORLD_MACHINE);
        if(!(entity instanceof Player) || !(itemUse instanceof String)) return;

        Player player = (Player) entity;

        if(!canInteract((Player) entity, zone, x, y)) return;

        player.showDialog(DialogHelper.getDialog("world_machines." + itemUse + ".menu"), ans -> {
            if(ans.length == 0 || !(ans[0] instanceof String)) return;
            switch ((String) ans[0]) {
                case "deactivate_natural_teleporters":
                    deactivateNaturalTeleporters(player, zone, x, y);
                    break;
                case "configure":
                    switch((String) itemUse) {
                        case "spawner":
                            zone.getMassSpawnerConfiguration().configure(player, zone, item);
                            break;
                        case "teleport":
                            zone.getMassTeleporterConfiguration().configure(player, zone, item);
                            break;
                        case "weather":
                            zone.getWeatherMachineConfiguration().configure(player, zone, item);
                            break;
                    }
                    break;
                case "move":
                    move(player, zone, metaBlock);
                    break;
                case "dismantle":
                    dismantle(player, zone, x, y);
                    break;
            }
        });
    }

    public boolean canInteract(Player player, Zone zone, int x, int y) {
        if(!player.isGodMode() && !zone.isOwner(player)) {
            player.notify("Sorry, you do not own this world.");
            return false;
        }

        if(zone.getBlock(x, y).getFrontMod() == 0) {
            player.notify("You need to supply the machine with steam first.");
            return false;
        }

        return true;
    }

    public void deactivateNaturalTeleporters(Player player, Zone zone, int x, int y) {
        if(canInteract(player, zone, x, y)) {
            for (MetaBlock metaBlock : zone.getMetaBlocksWithUse(ItemUseType.TELEPORT)) {
                if(!metaBlock.hasOwner() && !metaBlock.getItem().hasUse(ItemUseType.ZONE_TELEPORT)) {
                    zone.updateBlock(metaBlock.getX(), metaBlock.getY(), Layer.FRONT, Item.AIR);
                }
            }

            player.notify("Natural teleporters destroyed!");
        }
    }

    public void move(Player player, Zone zone, MetaBlock metaBlock) {
        player.setTransmittableBlock(metaBlock);
        player.notify("Place a beacon to move the machine. The machine's lower left corner will replace the beacon.");
    }

    public void dismantle(Player player, Zone zone, int x, int y) {
        if(canInteract(player, zone, x, y)) {
            player.notify("World machines can not yet be dismantled, but this feature is coming soon!");
        }
    }
}
