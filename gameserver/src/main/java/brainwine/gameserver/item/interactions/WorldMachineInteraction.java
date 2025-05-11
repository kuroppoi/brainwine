package brainwine.gameserver.item.interactions;

import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.WorldMachineConfiguration;
import brainwine.gameserver.zone.Zone;

public class WorldMachineInteraction implements ItemInteraction {

    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        Object itemUse = item.getUse(ItemUseType.WORLD_MACHINE);
        if(!(entity instanceof Player) || !(itemUse instanceof String)) return;

        Player player = (Player) entity;

        if(!zone.isOwner(player) && item.hasUse(ItemUseType.PUBLIC)) {
            if(canInteractPublicly(player, zone, item, x, y)) {
                switch((String)itemUse) {
                    case "holograph":
                        zone.getHolographConfiguration().interactPublicly(player, zone, item);
                        break;
                }
            }
            return;
        }

        if(!canInteract((Player) entity, zone, x, y)) return;

        player.showDialog(DialogHelper.getDialog("world_machines." + itemUse + ".menu"), ans -> {
            if(ans.length == 0 || !(ans[0] instanceof String)) return;

            WorldMachineConfiguration machine = null;
            switch((String) itemUse) {
                case "spawner":
                    machine = zone.getMassSpawnerConfiguration();
                    break;
                case "teleport":
                    machine = zone.getMassTeleporterConfiguration();
                    break;
                case "weather":
                    machine = zone.getWeatherMachineConfiguration();
                    break;
                case "holograph":
                    machine = zone.getHolographConfiguration();
                    break;
            }
            if(machine == null) return;

            switch ((String) ans[0]) {
                case "configure":
                    machine.configure(player, zone, item, x, y);
                    break;
                case "move":
                    move(player, zone, metaBlock);
                    break;
                case "dismantle":
                    dismantle(player, zone, x, y);
                    break;
                default:
                    machine.handleCommand(player, zone, item, (String)ans[0]);
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

    public boolean canInteractPublicly(Player player, Zone zone, Item item, int x, int y) {
        Object itemUse = item.getUse(ItemUseType.WORLD_MACHINE);
        if(!(itemUse instanceof String)) return false;

        // Only world machines with public use can be used publicly
        if(!item.hasUse(ItemUseType.PUBLIC)) return false;

        if(player.getZone() != zone) return false;

        if(zone.getBlock(x, y).getFrontMod() == 0) {
            player.notify("You need to supply the machine with steam first.");
            return false;
        }

        return true;
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
