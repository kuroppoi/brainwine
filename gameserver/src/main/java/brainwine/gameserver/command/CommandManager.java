package brainwine.gameserver.command;

import static brainwine.gameserver.player.NotificationType.SYSTEM;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import brainwine.gameserver.player.Player;

@SuppressWarnings("unchecked")
public class CommandManager {
    
    public static final String CUSTOM_COMMAND_PREFIX = "!"; // TODO configurable
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Command> commands = new HashMap<>();
    private static final Map<String, Command> aliases = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if(initialized) {
            logger.warn(SERVER_MARKER, "CommandManager is already initialized - skipping!");
            return;
        }
        
        registerCommands();
        initialized = true;
    }
    
    private static void registerCommands() {
        logger.info(SERVER_MARKER, "Registering commands ...");
        Reflections reflections = new Reflections("brainwine.gameserver.command");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CommandInfo.class);
        
        for(Class<?> clazz : classes) {
            if(!Command.class.isAssignableFrom(clazz)) {
                logger.warn(SERVER_MARKER, "Attempted to register non-command class {}", clazz.getSimpleName());
                continue;
            }
            
            registerCommand((Class<? extends Command>)clazz);
        }
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
        
        Command command = getCommand(commandName, true);
        
        if(command == null || !command.canExecute(executor)) {
            executor.notify("Unknown command. Type '/help' for a list of commands.", SYSTEM);
            return;
        }
        
        if(executor instanceof Player) {
            Player player = (Player)executor;
            logger.info(SERVER_MARKER, "{} used command '/{}'", player.getName(), commandName + (args.length == 0 ? "" : " " + String.join(" ", args)));
        }
        
        command.execute(executor, args);
    }
    
    public static void registerCommand(Class<? extends Command> type) {
        CommandInfo info = type.getAnnotation(CommandInfo.class);
        
        if(info == null) {
            logger.warn(SERVER_MARKER, "Cannot register command '{}' because it does not have the CommandInfo annotation", type.getSimpleName());
            return;
        }
        
        String name = info.name().toLowerCase();
        
        if(commands.containsKey(name)) {
            logger.warn(SERVER_MARKER, "Attempted to register duplicate command '{}' with name '{}'", type.getSimpleName(), name);
            return;
        }
        
        Command command = null;
        
        try {
            command = type.getConstructor().newInstance();
        } catch(ReflectiveOperationException e) {
            logger.error("Failed to not instantiate command '{}'", type.getSimpleName(), e);
            return;
        }
        
        commands.put(name, command);
        
        if(info.aliases() != null) {
            List<String> aliases = Stream.of(info.aliases()).map(String::toLowerCase).collect(Collectors.toList());
            
            for(String alias : aliases) {
                if(commands.containsKey(alias) || CommandManager.aliases.containsKey(alias)) {
                    logger.warn(SERVER_MARKER, "Duplicate alias {} for command {}", alias, command.getClass());
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
    
    public static Command getCommand(String name) {
        return getCommand(name, false);
    }
    
    public static Command getCommand(String name, boolean allowAlias) {
        return commands.getOrDefault(name.toLowerCase(), allowAlias ? aliases.get(name.toLowerCase()) : null);
    }
    
    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}
