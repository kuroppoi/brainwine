package brainwine.gameserver.quest;

import java.lang.IllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.messages.EventMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.shared.JsonHelper;

public class QuestAction {
    public static enum Type {
        INTERACT,
        BEGIN,
        DONE;
    }

    public static enum Actor {
        PLAYER,
        ANDROID;
    }

    private Actor actor = Actor.PLAYER;

    private String method;
    
    private List<Object> params = new ArrayList<>(0);

    @JsonSetter("params")
    public void setParams(Object params) {
        if(params instanceof List) this.params = (List<Object>)params;
        else this.params.add(params);
    }

    public Actor getActor() {
        return actor;
    }

    public String getMethod() {
        return method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void performAction(Player player) {
        try{
            switch(getMethod()) {
                case "gift_items!":
                    for(Object object : getParams()) {
                        Map<String, Integer> items = JsonHelper.readValue(object, new TypeReference<Map<String, Integer>>() {});
                        for(String k : items.keySet()) {
                            Item item = Item.get(k);
                            if(item == null) continue;
                            player.getInventory().addItem(item, items.get(k));
                            player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
                        }
                    }
                    break;
                case "event_message!":
                    if(params.size() < 2 || !(params.get(0) instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    player.sendMessage(new EventMessage((String) params.get(0), params.get(1)));
                    break;
                case "set_family_name!":
                    break;
                case "show_android_dialog":
                    String body = "No info.", title = null;
                    if(params.size() >= 1) {
                        if(!(params.get(0) instanceof String)) throw new IllegalArgumentException();
                        body = (String) params.get(0);
                    }

                    if(params.size() >= 2) {
                        if(!(params.get(0) instanceof String)) throw new IllegalArgumentException();
                        title = (String) params.get(0);
                    }

                    if(title == null) {
                        player.showDialog(DialogHelper.messageDialog(body));
                    } else {
                        player.showDialog(DialogHelper.messageDialog(title, body));
                    }
                    break;
                case "add_xp":
                    if(params.size() >= 1) {
                        int amount = JsonHelper.readValue(params.get(0), new TypeReference<Integer>() {});
                        player.addExperience(amount);
                    }
                    break;
                default:
                    player.notify(String.format("Unknown quest action %d", getMethod()));
            }
            } catch(JsonProcessingException e) {
                player.notify("Couldn't perform some actions for this quest due to JSON processing errors.");
            } catch(IllegalArgumentException e) {
                player.notify(String.format("Malformed quest action parameters for %d.", getMethod()));
            }
    }
    
}
