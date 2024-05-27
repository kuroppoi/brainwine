package brainwine.gameserver.shop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.gen.ZoneGenerator;

public class ZoneProduct extends Product {
    
    private final ZoneProductSettings settings;
    
    @JsonCreator
    public ZoneProduct(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "cost", required = true) int cost,
            @JsonProperty(value = "zone", required = true) ZoneProductSettings settings) {
        super(name, cost);
        this.settings = settings;
    }
    
    @Override
    public void purchase(Player player) {
        ZoneGenerator generator = settings.getGenerator() == null ? ZoneGenerator.getZoneGenerator(settings.getBiome()) : ZoneGenerator.getZoneGenerator(settings.getGenerator());
        
        // Refund purchase if generator doesn't exist
        if(generator == null) {
            player.notify("Oops! There was a problem with your purchase.");
            player.setCrowns(player.getCrowns() + cost);
            return;
        }
        
        player.showDialog(DialogHelper.messageDialog("World purchased!", "We've started generating your private world. We'll let you know when it's ready for exploration!"));
        generator.generateZoneAsync(settings.getBiome(), settings.getWidth(), settings.getHeight(), zone -> {
            // Refund purchase if world failed to generate
            if(zone == null) {
                player.showDialog(DialogHelper.messageDialog("There was a problem generating your private world. Your crowns have been refunded."));
                player.setCrowns(player.getCrowns() + cost);
                return;
            }
            
            // Update world ownership
            zone.setOwner(player);
            zone.setPrivate(true);
            zone.setProtected(true);
            GameServer.getInstance().getZoneManager().addZone(zone);
            
            // Ask player if they want to travel to their newly purchased world
            player.showDialog(DialogHelper.messageDialog("Your private world is ready!", String.format(
                    "Your private world '%s' is ready for exploration and adventure! Let's head there now.", zone.getName())).setActions("yesno"), input -> {
                // Check cancellation
                if(input.length == 1 && "cancel".equals(input[0])) {
                    return;
                }
                
                // Send player to purchased zone
                player.changeZone(zone);
            });
        });
    }
}
