package brainwine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import brainwine.api.DataFetcher;
import brainwine.api.models.ZoneInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

public class DirectDataFetcher implements DataFetcher {
    
    private final PlayerManager playerManager;
    private final ZoneManager zoneManager;
    
    public DirectDataFetcher(PlayerManager playerManager, ZoneManager zoneManager) {
        this.playerManager = playerManager;
        this.zoneManager = zoneManager;
    }
    
    @Override
    public boolean isPlayerNameTaken(String name) {
        return playerManager.getPlayer(name) != null;
    }

    @Override
    public String registerPlayer(String name) {
        return playerManager.register(name);
    }

    @Override
    public String login(String name, String password) {
        return playerManager.login(name, password);
    }
    
    @Override
    public String fetchPlayerName(String name) {
        Player player = playerManager.getPlayer(name);
        return player == null ? null : player.getName();
    }

    @Override
    public boolean verifyAuthToken(String name, String token) {
        return playerManager.verifyAuthToken(name, token);
    }
    
    @Override
    public boolean verifyApiToken(String apiToken) {
        return true; // TODO
    }
    
    @Override
    public Collection<ZoneInfo> fetchZoneInfo() {
        List<ZoneInfo> zoneInfo = new ArrayList<>();
        Collection<Zone> zones = zoneManager.getZones();
        
        for(Zone zone : zones) {
            zoneInfo.add(new ZoneInfo(zone.getName(), 
                    zone.getBiome().getId(), 
                    null,
                    false,
                    false,
                    zone.isPrivate(),
                    zone.isProtected(),
                    zone.getPlayers().size(), 
                    zone.getExplorationProgress(), 
                    zone.getCreationDate(),
                    zone.getOwner(),
                    zone.getMembers()));
        }
        
        return zoneInfo;
    }
}
