package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.WeatherManager;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "weather", description = "Displays or changes the weather in the current zone.")
public class WeatherCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Zone zone = ((Player)executor).getZone();
        WeatherManager weatherManager = zone.getWeatherManager();
        
        if(args.length < 1) {            
            if(weatherManager.isRaining()) {
                String rainString = zone.getBiome() == Biome.ARCTIC ? "snowing" : zone.getBiome() == Biome.HELL ? "raining ash" : "raining";
                executor.notify(String.format("It is currently %s in %s with an intensity of %s",
                        rainString, zone.getName(), weatherManager.getPrecipitation()), SYSTEM);
            } else {
                executor.notify(String.format("It is currently dry in %s.", zone.getName()), SYSTEM);
            }
            
            return;
        }
        
        boolean dry = args[0].equalsIgnoreCase("clear");
        zone.getWeatherManager().createRandomRain(dry);
        executor.notify(String.format("Weather has been %s in %s.", dry ? "cleared" : "made rainy", zone.getName()), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/weather [clear|rain]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
