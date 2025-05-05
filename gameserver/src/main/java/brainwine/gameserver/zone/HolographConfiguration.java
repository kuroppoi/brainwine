package brainwine.gameserver.zone;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.command.CommandAccessLevel;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.AppearanceSlot;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class HolographConfiguration extends WorldMachineConfiguration {
    @JsonProperty
    private CommandAccessLevel onOffAccessLevel = CommandAccessLevel.OWNERS;
    @JsonProperty
    private CommandAccessLevel changeOutfitAccessLevel = CommandAccessLevel.OWNERS;
    @JsonProperty
    private int timeout = 0;
    @JsonProperty
    private long outfitChangeTime = System.currentTimeMillis();
    @JsonProperty
    private final Map<String, Object> outfitOverrides = new HashMap<>();
    @JsonProperty
    private boolean turnedOn = false;

    @Override
    protected String getDialogName() {
        return "dialogs.world_machines.holograph.configure";
    }

    @Override
    protected String getPublicDialogName() {
        return "dialogs.world_machines.holograph.public";
    }

    @Override
    protected void configure(Zone zone, Map<String, Object> values) throws IllegalArgumentException {
        CommandAccessLevel[] accessLevels = CommandAccessLevel.values();
        if(values.containsKey("on_off")) {
            onOffAccessLevel = accessLevels[MathUtils.clamp(expectInteger(values.get("on_off")), 0, 2)];
        }

        if(values.containsKey("change_outfit")) {
            changeOutfitAccessLevel = accessLevels[MathUtils.clamp(expectInteger(values.get("change_outfit")), 0, 2)];
        }

        if(values.containsKey("timeout")) {
            timeout = MathUtils.clamp(expectInteger(values.get("timeout")), 0, 600_000);
        }
    }

    @Override
    public void handleCommand(Player player, Zone zone, Item item, String command) {
        if(System.currentTimeMillis() - outfitChangeTime < 500) {
            player.notify("You send commands too frequently. Please try again later.");
            return;
        }

        switch(command) {
            case "change_outfit":
                if(changeOutfitAccessLevel.isPrivileged(player, zone)) {
                    setOutfitFrom(player, zone, item.getPower());
                } else {
                    player.notify(
                            changeOutfitAccessLevel == CommandAccessLevel.OWNERS
                                    ? "Only the world owner is able to change the outfit of the holograph."
                                    : "Only the world members are able to change the outfit of the holograph."
                    );
                }
                break;
            case "turn_on":
            case "turn_off":
                if(onOffAccessLevel.isPrivileged(player, zone)) {
                    if ("turn_on".equals(command)) {
                        turnOnMachine(zone);
                    } else {
                        turnOffMachine(zone);
                    }
                } else {
                    player.notify(
                            changeOutfitAccessLevel == CommandAccessLevel.OWNERS
                                    ? "Only the world owner is able to turn " + command + " the holograph."
                                    : "Only the world members are able to turn " + command + " the holograph."
                    );
                }
                break;
        }
    }

    @Override
    protected Object getValue(String key) {
        switch(key) {
            case "on_off":
                return onOffAccessLevel.ordinal();
            case "change_outfit":
                return changeOutfitAccessLevel.ordinal();
            case "timeout":
                return Integer.toString(timeout);
            default:
                return null;
        }
    }

    public CommandAccessLevel getOnOffAccessLevel() {
        return onOffAccessLevel;
    }

    public CommandAccessLevel getChangeOutfitAccessLevel() {
        return changeOutfitAccessLevel;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public Map<String, Object> getOutfitOverrides() {
        return outfitOverrides;
    }

    public void tick(Zone zone, float deltaTime) {
        if(timeout != 0 && System.currentTimeMillis() > timeout + outfitChangeTime && turnedOn) {
            turnOffMachine(zone);
        }
    }

    public void turnOnMachine(Zone zone) {
        if(turnedOn) return;
        outfitChangeTime = System.currentTimeMillis();
        turnedOn = true;
        updateZoneAppearances(zone);
    }

    public void turnOffMachine(Zone zone) {
        if(!turnedOn) return;
        turnedOn = false;
        updateZoneAppearances(zone);
    }

    public void setOutfitFrom(Player player, Zone zone, float availablePower) {
        Map<String, Object> appearancePower = MapHelper.getMap(GameConfiguration.getBaseConfig(), "dialogs.world_machines.holograph.appearance", new HashMap<>());
        outfitOverrides.clear();
        Map<String, Object> outfit = player.getAppearance();
        for(AppearanceSlot slot : AppearanceSlot.values()) {
            if(expectInteger(appearancePower.getOrDefault(slot.getCategory(), 0)) < availablePower) {
                outfitOverrides.put(slot.getId(), outfit.get(slot.getId()));
            }
        }
        if(turnedOn) {
            updateZoneAppearances(zone);
        }
        outfitChangeTime = System.currentTimeMillis();
    }

    private void updateZoneAppearances(Zone zone) {
        for(Player player : zone.getPlayers()) {
            zone.spawnEffect(player.getX(), player.getY(), "bomb-teleport", 4);
            zone.sendMessage(new EntityChangeMessage(player.getId(), player.getVisibleAppearance()));
        }
    }

    public Map<String, Object> overrideAppearance(Map<String, Object> appearance) {
        if(turnedOn && !outfitOverrides.isEmpty()) {
            appearance = new HashMap<>(appearance);
            appearance.putAll(outfitOverrides);
            return appearance;
        } else {
            return appearance;
        }
    }
}
