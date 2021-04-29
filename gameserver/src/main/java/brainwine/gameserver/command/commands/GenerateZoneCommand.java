package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

public class GenerateZoneCommand extends Command {

    public static final int MIN_WIDTH = 200;
    public static final int MIN_HEIGHT = 200;
    public static final int MAX_WIDTH = 4000;
    public static final int MAX_HEIGHT = 1600;
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Biome biome = Biome.getRandomBiome();
        int width = 2000;
        int height = 800;
        int seed = (int)(Math.random() * Integer.MAX_VALUE);
        
        if(args.length > 0 && args.length < 2) {
            executor.sendMessage(String.format("Usage: %s", getUsage()));
            return;
        }
        
        if(args.length >= 2) {
            try {
                width = Integer.parseInt(args[0]);
                height = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                executor.sendMessage("Zone width and height must be valid numbers.");
                return;
            }
            
            if(width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
                executor.sendMessage(String.format("Zones must be between %sx%s and %sx%s blocks.", MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT));
                return;
            } else if(width % Zone.DEFAULT_CHUNK_WIDTH != 0 || height % Zone.DEFAULT_CHUNK_HEIGHT != 0) {
                executor.sendMessage(String.format("Zone size must be a multiple of %s", Zone.DEFAULT_CHUNK_WIDTH));
                return;
            }
        }
        
        if(args.length >= 3) {
            biome = Biome.fromName(args[2]);
        }
        
        if(args.length >= 4) {
            try {
                seed = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                seed = args[3].hashCode();
            }
        }
        
        executor.sendMessage("Your zone is being generated. It should be ready soon!");
        GameServer.getInstance().getZoneManager().generateZoneAsync(biome, width, height, seed, zone -> {
            executor.sendMessage(String.format("Your zone '%s' is ready for exploration!", zone.getName()));
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
    public String getUsage() {
        return "/genzone [<width> <height>] [biome] [seed]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
