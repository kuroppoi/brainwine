package brainwine.gameserver.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.command.commands.AdminCommand;
import brainwine.gameserver.command.commands.BroadcastCommand;
import brainwine.gameserver.command.commands.GiveCommand;
import brainwine.gameserver.command.commands.HelpCommand;
import brainwine.gameserver.command.commands.KickCommand;
import brainwine.gameserver.command.commands.PlayerIdCommand;
import brainwine.gameserver.command.commands.RegisterCommand;
import brainwine.gameserver.command.commands.SayCommand;
import brainwine.gameserver.command.commands.StopCommand;
import brainwine.gameserver.command.commands.TeleportCommand;
import brainwine.gameserver.command.commands.ThinkCommand;
import brainwine.gameserver.command.commands.ZoneIdCommand;
import brainwine.gameserver.entity.player.Player;

public class CommandManager {
    
    public static final String CUSTOM_COMMAND_PREFIX = "!"; // TODO configurable
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Command> commands = new HashMap<>();
    private static final Map<String, Command> aliases = new HashMap<>();
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
        registerCommand(new StopCommand());
        registerCommand(new RegisterCommand());
        registerCommand(new TeleportCommand());
        registerCommand(new KickCommand());
        registerCommand(new SayCommand());
        registerCommand(new ThinkCommand());
        registerCommand(new BroadcastCommand());
        registerCommand(new PlayerIdCommand());
        registerCommand(new ZoneIdCommand());
        registerCommand(new AdminCommand());
        registerCommand(new HelpCommand());
        registerCommand(new GiveCommand());
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
        if(!(executor instanceof Player) && commandName.startsWith(CUSTOM_COMMAND_PREFIX) || commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        
        Command command = commands.getOrDefault(commandName, aliases.get(commandName));
        
        if(command == null) {
            executor.sendMessage("Unknown command. Type '/help' for a list of commands.");
            return;
        }
        
        if(executor instanceof Player && command.requiresAdmin()) {
            if(!((Player)executor).isAdmin()) {
                executor.sendMessage("Sorry, you do not have the required permissions for this command.");
                return;
            }
        }
        
        command.execute(executor, args);
    }
    
    public static void registerCommand(Command command) {
        String name = command.getName();
        
       if(commands.containsKey(name)) {
           logger.warn("Attempted to register duplicate command {} with name {}", command.getClass(), name);
           return;
       }
       
       commands.put(name, command);
       String[] aliases = command.getAliases();
       
       if(aliases != null) {
           for(String alias : aliases) {
               if(commands.containsKey(alias) || CommandManager.aliases.containsKey(alias)) {
                   logger.warn("Duplicate alias {} for command {}", alias, command.getClass());
                   continue;
               }
               
               CommandManager.aliases.put(alias, command);
           }
       }
    }
    
    public static Set<String> getCommandNames() {
        Set<String> names = new HashSet<>();
        names.addAll(commands.keySet());
        names.addAll(aliases.keySet());
        return names;
    }
    
    public static Collection<Command> getCommands() {
        return commands.values();
    }
}
