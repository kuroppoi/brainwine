package brainwine.gameserver.server.commands.console;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;

public class RegisterCommand extends PlayerCommand {
    
    @Override
    public void process(Player player) {
        player.alert("Sorry, registration is currently not possible.");
        
        /*
        Map<String, Object> dialog = new HashMap<>();
        dialog.put("padding", 10);
        List<Map<String, Object>> sections = new ArrayList<>();
        Map<String, Object> sectionA = new HashMap<>();
        sectionA.put("title", "Register to save your progress!");
        sectionA.put("text", "Enter your email address and desired password to save your progress and log in from different devices.");
        sections.add(sectionA);
        Map<String, Object> sectionB = new HashMap<>();
        Map<String, Object> inputA = new HashMap<>();
        inputA.put("title", "Email");
        inputA.put("type", "text");
        inputA.put("key", "email");
        inputA.put("max", 128);
        sectionB.put("input", inputA);
        sections.add(sectionB);
        Map<String, Object> sectionC = new HashMap<>();
        Map<String, Object> inputB = new HashMap<>();
        inputB.put("title", "Password");
        inputB.put("type", "text");
        inputB.put("key", "password");
        inputB.put("max", 64);
        inputB.put("password", true);
        sectionC.put("input", inputB);
        sections.add(sectionC);
        dialog.put("sections", sections);
        player.sendMessage(new DialogMessage(123, dialog));
        */
    }
}
