package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;

import brainwine.gameserver.Fake;
import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.npc.behavior.BehaviorMessage;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.entity.npc.job.Job;
import brainwine.gameserver.entity.npc.job.jobs.Crafter;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

public class DialoguerBehavior extends Behavior {

    private static final long NEXT_DIALOGUE_OVERALL_MS = 10_000;

    private long mostRecentDialogueAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
    
    @JsonCreator
    public DialoguerBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        if (System.currentTimeMillis() < mostRecentDialogueAt + NEXT_DIALOGUE_OVERALL_MS) {
            entity.setAnimation(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void react(BehaviorMessage message, Player player, Object... data) {
        mostRecentDialogueAt = System.currentTimeMillis();

        switch(message) {
        case INTERACT:
            if (data.length <= 1) {
                if (entity.getJob() != null) {
                    Job.get(entity.getJob()).dialogue(entity, player);
                    return;
                }

                if(player.isAdmin()) {
                    DialoguerJob.CONFIGURATION_ONLY.dialogue(entity, player);
                } else {
                    // Respond with a random bogus message for now
        
                    String[] responses = {
                        "Error: Job module not found.",
                        "I am not quite ready for that yet.",
                        "Sorry, please try again later.",
                        "Query returned error 404.",
                        "Critical error.",
                        "Does not compute."
                    };
        
                    String response = responses[(int)(Math.random() * responses.length)];
                    entity.emote(response);
                }
            } else {
                switch ((String) data[0]) {
                    case "item":
                        int itemId = (int) data[1];
                        Item item = Item.get(itemId);

                        if (item == null) {
                            return;
                        }

                        if (item.hasUse(ItemUseType.MEMORY)) {
                            loadMemory(player, item);
                        } else {
                            if ("crafter".equals(entity.getJob())) {
                                ((Crafter) Job.get("crafter")).craftDialog(player, item);
                            }
                        }

                        return;
                    default:
                        return;
                }
            }
            break;
        default:
            break;
        }
    }

    public void loadMemory(Player player, Item item) {
        if(entity.getJob() == null) {
            if (player.getZone().isBlockProtected(entity.getBlockX(), entity.getBlockY(), player)) {
                String message = MapHelper.getString(GameConfiguration.getBaseConfig(), "dialogs.android.protected");
                player.showDialog(DialogHelper.messageDialog(message));
            } else {
                Integer memoryType = (Integer)item.getUse(ItemUseType.MEMORY);
                Map<String, Object> dialogDesc = (Map<String, Object>)MapHelper.getList(GameConfiguration.getBaseConfig(), "dialogs.android.load_memory").get(memoryType);
                try {
                    Dialog dialog = JsonHelper.readValue(dialogDesc, Dialog.class);

                    player.showDialog(dialog, ans -> {
                        if (ans.length >= 1 && ans[0] == "cancel") {
                            return;
                        }

                        if (ans.length >= 1) {
                            String entityName = (String)ans[0];

                            // remove the memory unit from the player's inventory
                            player.getInventory().removeItem(item, true);

                            // set entity parameters
                            entity.setName(entityName);
                            entity.setJob("quester");

                            // notify the player
                            player.notify(String.format("Android has been reconfigured as %s!", entity.getName()));

                            // update entity name on client side
                            entity.getZone().sendMessage(new EntityChangeMessage(entity.getId(), Map.of("n", entity.getName())));
                        }
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    player.showDialog(DialogHelper.messageDialog("Could not load the memory load dialog."));
                }
            }
        } else {
            List<String> options = MapHelper.getList(GameConfiguration.getBaseConfig(), "dialogs.android.cannot_load_memory");
            player.showDialog(DialogHelper.messageDialog(Fake.pickFromList(options)));
        }
    }
}
