package brainwine.gameserver.server;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

public interface Pusher {
    void handlePlayerJoin(Player player);
    void handlePlayerLeave(Player player);
    void handleZoneDiscovered(Zone zone);
    void handlePlayerMessage(Player player, String message);
}
