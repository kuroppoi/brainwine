package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 24)
public class ZoneChangeRequest extends PlayerRequest {
    
    public String zoneName;
    
    @Override
    public void process(Player player) {
        Zone zone = GameServer.getInstance().getZoneManager().getZoneByName(zoneName);
        
        if(zone == null) {
            player.notify("Sorry, could not find a zone with name " + zoneName);
            return;
        } else if(zone == player.getZone()) {
            player.notify("You're already in " + zoneName);
            return;
        }
        
        // Check survival requirement unless player has god mode enabled
        /*
        if(!player.isGodMode()) {
            Biome biome = zone.getBiome();
            int survival = MapHelper.getInt(GameConfiguration.getBaseConfig(), String.format("biomes.%s.survival_requirement", biome.getId()));
            
            if(player.getTotalSkillLevel(Skill.SURVIVAL) < survival) {
                player.notify(String.format("Your survival skill needs to be at least level %s to enter %s worlds.", survival, biome.getId()));
                return;
            }
        }*/
        
        player.changeZone(zone);
    }
}
