package brainwine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import brainwine.api.DataFetcher;
import brainwine.api.models.ZoneInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneActivity;
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
    public String fetchPlayerId(String apiToken) {
        Player player = playerManager.getPlayerByApiToken(apiToken);
        return player == null ? null : player.getDocumentId();
    }

    @Override
    public boolean verifyAuthToken(String name, String token) {
        return playerManager.verifyAuthToken(name, token);
    }
    
    @Override
    public ZoneInfo getZoneInfo(String nameOrId) {
        Zone zone = zoneManager.getZoneByName(nameOrId);
        
        if(zone == null) {
            zone = zoneManager.getZone(nameOrId);
        }
        
        return zone == null ? null : createZoneInfo(zone);
    }
    
    /**
     * TODO this will probably be slow if there is a large number of zones
     */
    @Override
    public Collection<ZoneInfo> fetchZoneInfo() {
        List<ZoneInfo> zoneInfo = new ArrayList<>();
        Collection<Zone> zones = zoneManager.getZones();
        
        for(Zone zone : zones) {
            zoneInfo.add(createZoneInfo(zone));
        }
        
        return zoneInfo;
    }

    @Override
    public Collection<ZoneInfo> fetchRecentZoneInfo(String apiToken) {
        Player player = playerManager.getPlayerByApiToken(apiToken);
        return player == null ? new ArrayList<>() : createZoneInfo(player.getRecentZones());
    }
    
    @Override
    public Collection<ZoneInfo> fetchBookmarkedZoneInfo(String apiToken) {
        Player player = playerManager.getPlayerByApiToken(apiToken);
        return player == null ? new ArrayList<>() : createZoneInfo(player.getBookmarkedZones());
    }
    
    private List<ZoneInfo> createZoneInfo(Collection<String> zoneIds) {
        return zoneIds.stream().map(zoneManager::getZone)
                .filter(Objects::nonNull)
                .map(DirectDataFetcher::createZoneInfo)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static ZoneInfo createZoneInfo(Zone zone) {
        return new ZoneInfo(zone.getName(),
                zone.getBiome().getId(),
                zone.getActivity() == null || zone.getActivity() == ZoneActivity.NONE ? null : zone.getActivity().toString().toLowerCase(),
                zone.isPvp(),
                false,
                zone.isPrivate(),
                zone.isProtected(),
                zone.getPlayerCount(),
                zone.getWidth(),
                zone.getHeight(),
                zone.getSurface(),
                zone.getExplorationProgress(),
                zone.getCreationDate(),
                zone.getOwner(),
                zone.getMembers());
    }
}
