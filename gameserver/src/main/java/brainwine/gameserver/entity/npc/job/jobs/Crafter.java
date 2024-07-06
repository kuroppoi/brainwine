package brainwine.gameserver.entity.npc.job.jobs;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.item.CraftingRequirement;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.LazyItemGetter;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;

public class Crafter extends DialoguerJob {

    @Override
    public DialogSection getMainDialogSection(Npc me, Entity other) {
        return new DialogSection()
            .setText(MapHelper.getString(GameConfiguration.getBaseConfig(), "dialogs.android.craft"))
            .setChoice("craft");
    }

    @Override
    public boolean handleDialogAnswers(Npc me, Entity other, Object[] ans) {
        if (ans.length >= 1 && "craft".equals(ans[0])) {
            ((Player)other).showDialog(
                DialogHelper.messageDialog(
                    me.getName(),
                    MapHelper.getString(GameConfiguration.getBaseConfig(), "dialogs.android.craft_response")
                )
            );
        }

        return true;
    }

    public boolean craftDialog(Entity other, Item item) {
        if(!other.isPlayer()) return false;
        Player player = (Player)other;

        // I can't craft this
        if(item.getCraft() == null || "android".equals(item.getCraft().getCrafter())) {
            player.showDialog(DialogHelper.messageDialog(MapHelper.getString(GameConfiguration.getBaseConfig(), "dialogs.android.cannot_craft")));
        }

        // Item is not craftable by an android
        if (item.getCraft() == null) {
            player.showDialog(DialogHelper.messageDialog("Sorry, that item doesn't have any crafting options."));
            return false;
        }

        Dialog dialog = new Dialog();
        dialog.addSection(new DialogSection().setText(String.format("I can use your %s to craft something if you have the supplies.", item.getTitle())));

        for (String itemId : item.getCraft().getOptions().keySet()) {
            Item optionItem = Item.get(itemId);

            if(optionItem != null) {
                dialog.addSection(new DialogSection().setText(optionItem.getTitle()).setChoice(itemId));
            } else {
                dialog.addSection(new DialogSection().setText(String.format("Unknown item: %s", itemId)));
            }
        }

        dialog.addSection(new DialogSection().setText("Never mind.").setChoice("cancel"));

        player.showDialog(dialog, ans -> continueCraftDialog(other, item, ans));

        return true;
    }

    public void continueCraftDialog(Entity other, Item item, Object[] ans) {
        if(!other.isPlayer()) return;
        Player player = (Player)other;

        if (ans.length >= 1 && "cancel".equals(ans[0])) {
            return;
        }

        Item craftItem = Item.get((String)ans[0]);

        if (craftItem == null) return;

        List<CraftingRequirement> requirements = new ArrayList<>(item.getCraft().getOptions().get((String)ans[0]));
        // add the dropped item as requirement so we check for it/remove it in the for loops
        requirements.add(new CraftingRequirement(new LazyItemGetter(item.getId()), 1));
        
        for(CraftingRequirement requirement : requirements) {
            if(!player.getInventory().hasItem(requirement.getItem(), requirement.getQuantity())) {
                player.showDialog(DialogHelper.messageDialog(String.format("Oops, you need more %s for me to craft that!", requirement.getItem().getTitle())));
                return;
            }
        }

        // all good, commit with crafting

        for(CraftingRequirement requirement : requirements) {
            player.getInventory().removeItem(requirement.getItem(), requirement.getQuantity());
        }

        player.getInventory().addItem(craftItem, 1, true);
    }
    
}
