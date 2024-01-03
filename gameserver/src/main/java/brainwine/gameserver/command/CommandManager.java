package brainwine.gameserver.command;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.command.commands.AcidityCommand;
import brainwine.gameserver.command.commands.AdminCommand;
import brainwine.gameserver.command.commands.BanCommand;
import brainwine.gameserver.command.commands.BroadcastCommand;
import brainwine.gameserver.command.commands.EntityCommand;
import brainwine.gameserver.command.commands.ExperienceCommand;
import brainwine.gameserver.command.commands.ExportCommand;
import brainwine.gameserver.command.commands.GenerateZoneCommand;
import brainwine.gameserver.command.commands.GiveCommand;
import brainwine.gameserver.command.commands.HealthCommand;
import brainwine.gameserver.command.commands.HelpCommand;
import brainwine.gameserver.command.commands.ImportCommand;
import brainwine.gameserver.command.commands.KickCommand;
import brainwine.gameserver.command.commands.LevelCommand;
import brainwine.gameserver.command.commands.MuteCommand;
import brainwine.gameserver.command.commands.PlayerIdCommand;
import brainwine.gameserver.command.commands.PositionCommand;
import brainwine.gameserver.command.commands.PrefabListCommand;
import brainwine.gameserver.command.commands.RegisterCommand;
import brainwine.gameserver.command.commands.RickrollCommand;
import brainwine.gameserver.command.commands.SayCommand;
import brainwine.gameserver.command.commands.SeedCommand;
import brainwine.gameserver.command.commands.SettleLiquidsCommand;
import brainwine.gameserver.command.commands.SkillPointsCommand;
import brainwine.gameserver.command.commands.StopCommand;
import brainwine.gameserver.command.commands.TeleportCommand;
import brainwine.gameserver.command.commands.ThinkCommand;
import brainwine.gameserver.command.commands.TimeCommand;
import brainwine.gameserver.command.commands.UnbanCommand;
import brainwine.gameserver.command.commands.UnmuteCommand;
import brainwine.gameserver.command.commands.WeatherCommand;
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
            logger.warn(SERVER_MARKER, "CommandManager is already initialized - skipping!");
            return;
        }
        
        registerCommands();
        initialized = true;
    }
    
    private static void registerCommands() {
        logger.info(SERVER_MARKER, "Registering commands ...");
        registerCommand(new StopCommand());
        registerCommand(new RegisterCommand());
        registerCommand(new TeleportCommand());
        registerCommand(new KickCommand());
        registerCommand(new MuteCommand());
        registerCommand(new UnmuteCommand());
        registerCommand(new BanCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new SayCommand());
        registerCommand(new ThinkCommand());
        registerCommand(new BroadcastCommand());
        registerCommand(new PlayerIdCommand());
        registerCommand(new ZoneIdCommand());
        registerCommand(new AdminCommand());
        registerCommand(new HelpCommand());
        registerCommand(new GiveCommand());
        registerCommand(new GenerateZoneCommand());
        registerCommand(new SeedCommand());
        registerCommand(new PrefabListCommand());
        registerCommand(new ExportCommand());
        registerCommand(new ImportCommand());
        registerCommand(new PositionCommand());
        registerCommand(new RickrollCommand());
        registerCommand(new EntityCommand());
        registerCommand(new HealthCommand());
        registerCommand(new ExperienceCommand());
        registerCommand(new LevelCommand());
        registerCommand(new SkillPointsCommand());
        registerCommand(new SettleLiquidsCommand());
        registerCommand(new WeatherCommand());
        registerCommand(new AcidityCommand());
        registerCommand(new TimeCommand());
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
    
    public static void registerCommand(Command command) {
        String name = command.getName();
        
       if(commands.containsKey(name)) {
           logger.warn(SERVER_MARKER, "Attempted to register duplicate command {} with name {}", command.getClass(), name);
           return;
       }
       
       commands.put(name, command);
       String[] aliases = command.getAliases();
       
       if(aliases != null) {
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
        return commands.getOrDefault(name, allowAlias ? aliases.get(name) : null);
    }
    
    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}
