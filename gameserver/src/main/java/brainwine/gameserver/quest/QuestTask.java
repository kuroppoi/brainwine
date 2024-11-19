package brainwine.gameserver.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;

public class QuestTask {
    @JsonProperty("desc")
    private String description;

    @JsonProperty("quantity")
    private int quantity = 1;

    @JsonProperty("events")
    private List<List<Object>> events = new ArrayList<>();

    @JsonProperty("progress")
    private List<List<Object>> progress = new ArrayList<>();

    @JsonProperty("qualify")
    private List<List<Object>> qualify = new ArrayList<>();

    @JsonProperty("action")
    private String action;

    @JsonProperty("collect_inventory")
    private QuestTaskCollectInventory collectInventory;

    public boolean checkQualification(Player player, Object... qualification) {
        if(player == null || qualification == null || qualification.length == 0) {
            return false;
        }

        if(Objects.equals("pvp?", qualification[0])) {
            return true; // TODO: change when PVP is supported.
        }

        if(Objects.equals("current_biome?", qualification[0])) {
            return qualification.length >= 2 && Objects.equals(qualification[1], player.getZone().getBiome().getId());
        }

        if(Objects.equals("current_item_group?", qualification[0])) {
            return qualification.length >= 2 && Objects.equals(qualification[1], player.getHeldItem().getId());
        }

        return true;
    }

    public boolean doesQualify(Player player) {
        if(this.getQualify() == null) return true;
        
        for(List<Object> qualification : this.getQualify()) {
            if(!checkQualification(player, qualification)) return false;
        }

        return true;
    }

    public String getDescription() {
        return description;
    }

    public QuestTask setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public QuestTask setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public List<List<Object>> getEvents() {
        return events;
    }

    public QuestTask setEvents(List<List<Object>> events) {
        this.events = events;
        return this;
    }

    public List<List<Object>> getProgress() {
        return progress;
    }

    public QuestTask setProgress(List<List<Object>> progress) {
        this.progress = progress;
        return this;
    }

    public List<List<Object>> getQualify() {
        return qualify;
    }

    public QuestTask setQualify(List<List<Object>> qualify) {
        this.qualify = qualify;
        return this;
    }

    public String getAction() {
        return action;
    }

    public QuestTask setAction(String action) {
        this.action = action;
        return this;
    }

    public QuestTaskCollectInventory getCollectInventory() {
        return collectInventory;
    }

    public QuestTask setCollectInventory(QuestTaskCollectInventory collectInventory) {
        this.collectInventory = collectInventory;
        return this;
    }

    public DialogSection getDialogSection(int taskProgress) {
        DialogSection result = new DialogSection();

        result.setText(getDescription() + (taskProgress >= 0 ? String.format("(Progress: %d/%d)", taskProgress, getQuantity()) : ""));
        
        if(!getQualify().isEmpty()) {
            result.addItem(new DialogListItem().setText("Qualifications:"));
            for(List<Object> qualification : getQualify()) {
                result.addItem(new DialogListItem().setText(qualification.stream().<String>map(Objects::toString).collect(Collectors.joining(" "))));
            }
        }

        if(!getEvents().isEmpty()) {
            result.addItem(new DialogListItem().setText("Do any of these to make progress:"));
            for(List<Object> event : getEvents()) {
                result.addItem(new DialogListItem().setText(event.stream().<String>map(Objects::toString).collect(Collectors.joining(" "))));
            }
        }

        if(getCollectInventory() != null && !getCollectInventory().getRequirements().isEmpty()) {
            result.addItem(new DialogListItem().setText("Things To Collect:"));

            getCollectInventory().addDialogListItems(result);
        }

        return result;

    }

}
