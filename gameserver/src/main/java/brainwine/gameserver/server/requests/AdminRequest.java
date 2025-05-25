package brainwine.gameserver.server.requests;

import java.util.Collection;
import java.util.stream.Collectors;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

@RequestInfo(id = 254)
public class AdminRequest extends PlayerRequest {
    
    public String key;
    
    @OptionalField
    public Object data;
    
    @Override
    public void process(Player player) {
        if(!player.isAdmin()) {
            return;
        }
        
        switch(key) {
            case "god":
                player.setGodMode(data == null || data.equals(1));
                break;
            case "admin":
                // This is a client-sided fuck-up
                if(player.isV3()) {
                    key = "grow";
                }
            default:
                // Delegate request to the command manager
                if(data == null) {
                    CommandManager.executeCommand(player, String.format("/%s", key));
                } else {
                    String parameters = data instanceof Collection<?> ? String.join(" ", 
                            ((Collection<?>)data).stream().map(String::valueOf).collect(Collectors.toList())) : String.valueOf(data);
                    CommandManager.executeCommand(player, String.format("/%s %s", key, parameters));
                }
                break;
        }
    }
}
