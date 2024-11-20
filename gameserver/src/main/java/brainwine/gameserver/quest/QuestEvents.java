package brainwine.gameserver.quest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

public class QuestEvents {
    private static boolean patternMatch(List<Object> event, Object[] pattern) {
        if(event.size() != pattern.length) return false;
        int iterationCount = Math.min(event.size(), pattern.length);
        for(int i = 0; i < iterationCount; i++) {
            if(pattern[i] == null) continue;
            if(event.get(i) == null) continue;

            if(!Objects.equals(pattern[i], event.get(i))) return false;
        }

        return true;
    }

    public static void handleEvent(Player player, Object... pattern) {
        handleEventWithQuantity(player, 1, pattern);
    }

    public static void handleEventWithQuantity(Player player, int quantity, Object... pattern) {
        for(Map.Entry<String, QuestProgress> questProgressEntry : player.getQuestProgresses().entrySet()) {
            String questId = questProgressEntry.getKey();
            QuestProgress questProgress = questProgressEntry.getValue();
            if(questProgress.isComplete()) continue;

            Quest quest = Quests.get(player, questId);
            int i = 0;

            boolean anyProgress = false;
            if(pattern != null) for(QuestTask task : quest.getTasks()) {
                if(task.getEvents() == null) continue;

                if(!task.doesQualify(player)) {
                    continue;
                }

                for(List<Object> event : task.getEvents()) {
                    try {
                        if(patternMatch(event, pattern)) {
                            questProgress.getTaskProgresses().set(i, questProgress.getTaskProgress(i) + quantity);

                            anyProgress = true;
                            break;
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                i++;
            }

            if(anyProgress) {
                PlayerQuests.sendPlayerQuestMessage(player, questProgress);
            }

            if(PlayerQuests.canFinishQuest(player, quest)) {
                PlayerQuests.finishQuest(player, quest);
                PlayerQuests.performAction(player, quest, QuestAction.Type.DONE);
            }

        }
    }

    public static  void handleCollectItem(Player player, Item item, int quantity) {
        handleEventWithQuantity(player, quantity, "collect_item", "id", item.getId(), quantity);
    }

    public static void handleCraft(Player player, Item item, int quantity) {
        handleEventWithQuantity(player, quantity, "craft", "code", item.getCode());
    }

    public static void handleEnterZone(Player player, Zone zone) {
        handleEvent(player, "entered", "zone_name", zone.getName());
    }

    public static void handleKill(Player player, Entity other) {
        handleEvent(player, "kill");
        handleEvent(player, "kill", "code", other.getType());

        if(other.isPlayer()) {
            // don't reward players for killing each other
        } else {
            Npc npc = (Npc) other;
            handleEvent(player, "kill", "category", npc.getConfig().getCategory());
        }
        
    }

    public static void handleExplode(Player player, Entity other) {
        handleEvent(player, "explode");
        handleEvent(player, "explode", "code", other.getType());

        if(other.isPlayer()) {
            // don't reward players for killing each other
        } else {
            Npc npc = (Npc) other;
            handleEvent(player, "explode", "category", npc.getConfig().getCategory());
        }
    }

    public static void handleChat(Player player) {
        handleEvent(player, "chat");
    }

    public static void handleAppearance(Player player, Map<String, Object> appearance) {
        for (Object code : appearance.values()) {
            handleEvent(player, "appearance", "code", code);
        }
    }

    public static void handleInteract(Player player, Npc npc) {
        handleEvent(player, "interact", "name", npc.getName());
    }
}
