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
    public static void handleQuestFinalReturn(Player player, Quest quest) {
        QuestProgress progress = player.getQuestProgresses().get(quest.getId());

        if(progress == null) return;

        int found = -1;

        for(int i = 0; i < quest.getTasks().size(); i++) {
            QuestTask task = quest.getTasks().get(i);

            if(task.getEvents() != null) {
                for(List<Object> event : task.getEvents()) {
                    for(Object o : event) {
                        if("return".equals(o)) found = i;
                        if(found != -1) break;
                    }
                    if(found != -1) break;
                }
            }
            if(found != -1) break;
        }

        if(found == -1) return;

        int initialReturnProgress = progress.getTaskProgress(found);
        int wantedProgress = quest.getTasks().get(found).getQuantity();

        while (progress.getTaskProgresses().size() < quest.getTasks().size()) {
            progress.getTaskProgresses().add(0);
        }

        progress.getTaskProgresses().set(found, wantedProgress);

        // if can't finish even with the return task done, set the return task progress to the previous value
        if(!PlayerQuests.canFinishQuest(player, quest)) {
            progress.getTaskProgresses().set(found, initialReturnProgress);
        }
    }

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
        for(Map.Entry<String, QuestProgress> questProgressEntry : player.getQuestProgresses().entrySet()) {
            String questId = questProgressEntry.getKey();
            QuestProgress questProgress = questProgressEntry.getValue();
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
                            if(event.size() >= 4 && "collect_item".equals(event.get(0)) && "id".equals(event.get(1))) {
                                Integer amount = (Integer) pattern[3];
                                if(amount != null && amount > 0) {
                                    questProgress.getTaskProgresses().set(i, questProgress.getTaskProgress(i) + amount);
                                }
                                anyProgress = true;
                                break;
                            }

                            questProgress.getTaskProgresses().set(i, questProgress.getTaskProgress(i) + 1);

                            if(event.size() >= 3 && "interact".equals(event.get(0)) && "name".equals(event.get(1))) {
                                PlayerQuests.performAction(player, quest, QuestAction.Type.INTERACT);
                            }

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

    public static  void handleCollectInventory(Player player, Item item, int quantity) {
        handleEvent(player, "collect_item", "id", item.getId(), quantity);
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

    public static void handleInteract(Player player, Npc npc) {
        handleEvent(player, "interact", "name", npc.getName());
    }

    public static void handleAppearance(Player player, Map<String, Object> appearance) {
        for(Object code : appearance.values()) {
            handleEvent(player, "appearance", "code", code);
        }
    }
}
