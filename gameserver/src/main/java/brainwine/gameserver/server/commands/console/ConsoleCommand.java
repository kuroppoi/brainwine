package brainwine.gameserver.server.commands.console;

import java.lang.reflect.Field;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 47)
public class ConsoleCommand extends PlayerCommand {
    
    public String command;
    public String[] arguments;
    
    @Override
    public void process(Player player) {        
        // TODO make proper command map
        switch(command) {
        case "tp":
            executeCommand(player, new TeleportCommand());
            break;
        case "register":
            executeCommand(player, new brainwine.gameserver.server.commands.console.RegisterCommand()); // Ahh, how cruel.
            break;
        }
    }
    
    /**
     * Ugly function but it'll have to do for now.
     */
    private void executeCommand(Player player, PlayerCommand command) {
        Field[] fields = command.getClass().getFields();
        
        if(arguments.length != fields.length) {
            player.alert("Incorrect parameters for this command.");
            return;
        }
        
        for(int i = 0; i < fields.length; i++) {
            String argument = arguments[i];
            Field field = fields[i];
            Class<?> type = field.getType();
            Object value = null;
            
            try {
                if(type == String.class) {
                    value = argument;
                } else if(type == int.class) {
                    value = Integer.parseInt(argument);
                }
            } catch(NumberFormatException e) {
                player.alert(String.format("Parameter '%s' must be a valid number.", field.getName()));
                return;
            }
            
            try {
                field.set(command, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                player.alert("An internal error occured.");
                e.printStackTrace();
                return;
            }
        }
        
        command.process(player);
    }
}
