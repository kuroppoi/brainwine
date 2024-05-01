package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.gen.ZoneGenerator;

@CommandInfo(name = "genzone", description = "Asynchronously generates a new zone.", aliases = "generate")
public class GenerateZoneCommand extends Command {

    public static final int MIN_WIDTH = 200;
    public static final int MIN_HEIGHT = 200;
    public static final int MAX_WIDTH = 4000;
    public static final int MAX_HEIGHT = 1600;
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Biome biome = args.length > 0 ? Biome.fromName(args[0]) : Biome.getRandomBiome();
        int width = biome == Biome.DEEP ? 1200 : 2000;
        int height = biome == Biome.DEEP ? 1000 : 600;
        int seed = (int)(Math.random() * Integer.MAX_VALUE);
        
        if(args.length > 1 && args.length < 3) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        if(args.length >= 3) {
            try {
                width = Integer.parseInt(args[1]);
                height = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                executor.notify("Zone width and height must be valid numbers.", SYSTEM);
                return;
            }
            
            if(width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
                executor.notify(String.format("Zones must be between %sx%s and %sx%s blocks.",
                        MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT), SYSTEM);
                return;
            } else if(width % Zone.DEFAULT_CHUNK_WIDTH != 0 || height % Zone.DEFAULT_CHUNK_HEIGHT != 0) {
                executor.notify(String.format("Zone size must be a multiple of %s", Zone.DEFAULT_CHUNK_WIDTH), SYSTEM);
                return;
            }
        }
        
        ZoneGenerator generator = null;
        
        if(args.length >= 4) {
            String name = args[3];
            generator = ZoneGenerator.getZoneGenerator(name);
            
            if(generator == null) {
                executor.notify(String.format("The zone generator '%s' does not exist.", name), SYSTEM);
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
        
        executor.notify("Your zone is being generated. It should be ready soon!", SYSTEM);
        generator.generateZoneAsync(biome, width, height, seed, zone -> {
            if(zone == null) {
                executor.notify("An unexpected error occured while generating your zone.", SYSTEM);
            } else {
                GameServer.getInstance().getZoneManager().addZone(zone);
                executor.notify(String.format("Your zone '%s' is ready for exploration!", zone.getName()), SYSTEM);
            }
        });
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/genzone [biome] [<width> <height>] [generator] [seed]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
