package brainwine.gameserver.item.interactions;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.dialog.input.DialogTextIndexInput;
import brainwine.gameserver.dialog.input.DialogTextInput;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.PickRandom;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.dynamics.EvokerInvasion;
import brainwine.gameserver.zone.dynamics.SummonedInvasion;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SummoningCircleInteraction implements ItemInteraction {
    static final List<String> words = Arrays.asList("lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua enim ad minim veniam quis nostrud exercitation ullamco laboris nisi aliquip ex ea commodo consequat duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur excepteur sint occaecat cupidatat non proident sunt in culpa qui officia deserunt mollit anim id est laborum".split(" "));
    @Override
    public void interact(Zone zone, Entity entity, int x, int y, Layer layer, Item item, int mod, MetaBlock metaBlock, Object config, Object[] data) {
        if(!entity.isPlayer()) return;
        Player player = (Player)entity;

        long configCooldown = 0;
        boolean configUseSpell = false;

        if(config instanceof Map) {
            configCooldown = MapHelper.getLong((Map<?, ?>) config, "cooldown", 0L);
            configUseSpell = MapHelper.getBoolean((Map<?, ?>)config, "spell", false);
        }

        long cooldown = configCooldown;
        boolean useSpell = !player.isGodMode() && configUseSpell;

        if (cooldown > 0 && !player.isGodMode()) {
            String t = metaBlock != null ? metaBlock.getStringProperty("t") : null;
            if (t != null && OffsetDateTime.parse(t).plus(cooldown, ChronoUnit.MILLIS).isAfter(OffsetDateTime.now())) {
                player.notify("This item is on cooldown.");
                return;
            }
        }

        String spell = useSpell ? String.join(" ", PickRandom.sampleWithoutReplacement(words, 3 + (int) (8 * Math.random()))) : "";

        Dialog dialog = new Dialog().addSection(new DialogSection().setTitle("Summoning"));
        if(useSpell) {
            dialog
                    .addSection(new DialogSection().setTitle("Type out the following spell correctly."))
                    .addSection(new DialogSection().setText(spell))
                    .addSection(new DialogSection().setInput(new DialogTextInput().setMaxLength(spell.length() + 5).setKey("spell")));
        }

        dialog.addSection(new DialogSection().setInput(new DialogSelectInput().setOptions("Revenants", "Revenant Lord").setKey("difficulty")));

        dialog.addSection(new DialogSection().setText("Which players should be cursed?"));
        for(Player other : zone.getPlayers()) {
            dialog.addSection(new DialogSection()
                    .setText(other.getName())
                    .setInput(new DialogTextIndexInput()
                            .setOptions("No", "Yes")
                            .setKey("player." + other.getName())
                            .setValue(player == other ? 0 : 1)
                    )
            );
        }

        player.showDialog(dialog, ans -> {
            boolean accepted = false;
            String option = "Revenants";
            String inputtedSpell = "";
            List<Player> players = new ArrayList<>();

            if(ans.length == 0 || "cancel".equals(ans[0])) {
                return;
            }

            int counter = 0;
            for(DialogSection section : dialog.getSections()) {
                if(counter >= ans.length) {
                    player.notify("Invalid input!");
                    return;
                }

                if(section.getInput() != null) {
                    if("difficulty".equals(section.getInput().getKey())) {
                        if(ans[counter] instanceof String) option = (String)ans[counter];
                        counter++;
                        continue;
                    }

                    if("spell".equals(section.getInput().getKey())) {
                        if(ans[counter] instanceof String) inputtedSpell = (String)ans[counter];
                        counter++;
                        continue;
                    }

                    if(section.getInput().getKey() != null && section.getInput().getKey().startsWith("player.")) {
                        if(Objects.equals(1, ans[counter])) {
                            String username = section.getInput().getKey().substring("player.".length());
                            Player other = zone.getPlayer(username);
                            if(other != null) {
                                players.add(other);
                            }
                        }
                        counter++;
                    }
                }
            }

            if(players.isEmpty()) {
                player.notify("OK, not cursing anyone.");
                return;
            }

            if(useSpell) {
                if(spell.equalsIgnoreCase(inputtedSpell)) {
                    accepted = true;
                }
            } else {
                accepted = true;
            }

            int numWaves = 2 + (int) (4 * Math.random());
            int difficulty = 3;
            if("Revenant Lord".equals(option)) {
                difficulty = 5;
                numWaves = 1;
            }

            if(accepted) {
                player.notify("Summoning " + option);
                zone.getDynamicsManager().beginDynamic(new SummonedInvasion(zone, new ArrayList<>(players), difficulty, numWaves));
                if(cooldown > 0) {
                    MetaBlock m = zone.getMetaBlock(x, y);
                    Map<String, Object> metadata = m != null ? m.getMetadata() : new HashMap<>();
                    metadata.put("t", OffsetDateTime.now().toString());
                    zone.setMetaBlock(x, y, item, null, metadata);
                }
            } else {
                player.notify("Too bad, you casted the wrong spell!");
                zone.getDynamicsManager().beginDynamic(
                        new EvokerInvasion(zone, player, zone.getMassSpawnerConfiguration().getDifficulty(), numWaves)
                );
            }
        });
    }
}
