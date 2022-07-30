package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;
import brainwine.gameserver.zone.gen.ZoneGenerator;

public class GenerateZoneCommand extends Command {

    public static final int MIN_WIDTH = 200;
    public static final int MIN_HEIGHT = 200;
    public static final int MAX_WIDTH = 4000;
    public static final int MAX_HEIGHT = 1600;
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Biome biome = Biome.getRandomBiome();
        int width = 2000;
        int height = 600;
        int seed = (int)(Math.random() * Integer.MAX_VALUE);
        
        if(args.length > 0 && args.length < 2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        if(args.length >= 2) {
            try {
                width = Integer.parseInt(args[0]);
                height = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                executor.notify("Zone width and height must be valid numbers.", ALERT);
                return;
            }
            
            if(width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
                executor.notify(String.format("Zones must be between %sx%s and %sx%s blocks.", MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT), ALERT);
                return;
            } else if(width % Zone.DEFAULT_CHUNK_WIDTH != 0 || height % Zone.DEFAULT_CHUNK_HEIGHT != 0) {
                executor.notify(String.format("Zone size must be a multiple of %s", Zone.DEFAULT_CHUNK_WIDTH), ALERT);
                return;
            }
        }
        
        if(args.length >= 3) {
            biome = Biome.fromName(args[2]);
        }
        
        ZoneGenerator generator = null;
        
        if(args.length >= 4) {
            String name = args[3];
            generator = ZoneGenerator.getZoneGenerator(name);
            
            if(generator == null) {
                executor.notify(String.format("The zone generator '%s' does not exist.", name), ALERT);
                return;
            }
        } else {
            generator = ZoneGenerator.getZoneGenerator(biome);
            
            // If no custom generator was specified, use the default one.
            if(generator == null){
                generator = ZoneGenerator.getDefaultZoneGenerator();
            }
        }
        
        if(args.length >= 5) {
            try {
                seed = Integer.parseInt(args[4]);
            } catch(NumberFormatException e) {
                seed = args[4].hashCode();
            }
        }
        
        executor.notify("Your zone is being generated. It should be ready soon!", ALERT);
        generator.generateZoneAsync(biome, width, height, seed, zone -> {
            if(zone == null) {
                executor.notify("An unexpected error occured while generating your zone.", ALERT);
            } else {
                ZoneManager zoneManager = GameServer.getInstance().getZoneManager();
                zoneManager.addZone(zone);
                zoneManager.saveZone(zone);
                executor.notify(String.format("Your zone '%s' is ready for exploration!", zone.getName()), ALERT);
            }
        });
    }

    @Override
    public String getName() {
        return "genzone";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "generate" };
    }
    
    @Override
    public String getDescription() {
        return "Asynchronously generates a new zone.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/genzone [<width> <height>] [biome] [generator] [seed]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
