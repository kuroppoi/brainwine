package brainwine.gameserver.server;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

public class DefaultPusher implements Pusher {
    @Override
    public void handlePlayerJoin(Player player) {}

    @Override
    public void handlePlayerLeave(Player player) {}

    @Override
    public void handleZoneDiscovered(Zone zone) {}

    @Override
    public void handlePlayerMessage(Player player, String message) {}
}
