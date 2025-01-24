package brainwine;

import brainwine.api.Api;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.Pusher;
import brainwine.gameserver.zone.Zone;

import java.util.HashMap;
import java.util.Map;

public class DirectPusher implements Pusher {
    Api api;

    public DirectPusher(Api api) {
        this.api = api;
    }

    private void broadcast(String type, Object data) {
        if(api != null) api.broadcast(type, data);
    }

    @Override
    public void handlePlayerJoin(Player player) {
        broadcast("player_joined", player.getStatusConfig());
    }

    @Override
    public void handlePlayerLeave(Player player) {
        broadcast("player_left", player.getStatusConfig());
    }

    @Override
    public void handleZoneDiscovered(Zone zone) {
        broadcast("zone_discovered", DirectDataFetcher.createZoneInfo(zone));
    }

    // Open to discussion
    @Override
    public void handlePlayerMessage(Player player, String message) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("player", player.getStatusConfig());
        msg.put("message", message);
        broadcast("player_message", msg);
    }
}
