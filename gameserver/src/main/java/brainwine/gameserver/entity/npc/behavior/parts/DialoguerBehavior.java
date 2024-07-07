package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.npc.behavior.BehaviorMessage;
import brainwine.gameserver.entity.npc.job.DialoguerJob;
import brainwine.gameserver.entity.npc.job.Job;
import brainwine.gameserver.entity.npc.job.jobs.Crafter;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.player.Player;

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

    public boolean loadMemory(Player player, Item item) {
        return false;
    }
}
