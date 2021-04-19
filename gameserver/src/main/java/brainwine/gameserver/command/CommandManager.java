package brainwine.gameserver.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.command.commands.BroadcastCommand;
import brainwine.gameserver.command.commands.KickCommand;
import brainwine.gameserver.command.commands.RegisterCommand;
import brainwine.gameserver.command.commands.SayCommand;
import brainwine.gameserver.command.commands.StopCommand;
import brainwine.gameserver.command.commands.TeleportCommand;
import brainwine.gameserver.command.commands.ThinkCommand;
import brainwine.gameserver.entity.player.Player;

public class CommandManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Command> commands = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if(initialized) {
            logger.warn("init() called twice");
            return;
        }
        
        registerCommands();
        initialized = true;
    }
    
    private static void registerCommands() {
        logger.info("Registering commands ...");
        registerCommand("stop", new StopCommand());
        registerCommand("register", new RegisterCommand());
        registerCommand("tp", new TeleportCommand());
        registerCommand("kick", new KickCommand());
        registerCommand("say", new SayCommand());
        registerCommand("think", new ThinkCommand());
        registerCommand("bc", new BroadcastCommand());
    }
    
    public static void executeCommand(CommandExecutor executor, String commandLine) {
        if(commandLine.isEmpty()) {
            return;
        }
        
        commandLine.trim().replaceAll(" +", " ");
        String[] sections = commandLine.split(" ", 2);
        
        if(sections.length == 0) {
            return;
        }
        
        String name = sections[0];
        String[] args = sections.length > 1 ? sections[1].split(" ") : new String[0];
        executeCommand(executor, name, args);
    }
    
    public static void executeCommand(CommandExecutor executor, String commandName, String[] args) {
        Command command = commands.get(commandName);
        
        if(command == null) {
            executor.alert("Sorry, that command does not exist.");
            return;
        }
        
        if(executor instanceof Player && command.requiresAdmin()) {
            if(!((Player)executor).isAdmin()) {
                executor.alert("Sorry, you do not have the required permissions for this command.");
                return;
            }
        }
        
        command.execute(executor, args);
    }
    
    public static void registerCommand(String name, Command command) {
       if(commands.containsKey(name)) {
           logger.warn("Attempted to register duplicate command {} with name {}", command.getClass(), name);
           return;
       }
       
       commands.put(name, command);
    }
    
    public static Set<String> getCommandNames() {
        return commands.keySet();
    }
}
