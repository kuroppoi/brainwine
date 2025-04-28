package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

public enum CommandAccessLevel {
    OWNERS,
    MEMBERS,
    EVERYONE;

    public boolean isPrivileged(CommandExecutor executor, Zone zone) {
        if(executor == null || zone == null) return false;
        if(executor instanceof GameServer) return true;
        if(executor.isAdmin()) return true;

        Player player = (Player)executor;
        if(this == CommandAccessLevel.OWNERS) return zone.isOwner(player);
        if(this == CommandAccessLevel.MEMBERS) return zone.isOwner(player) || zone.isMember(player);

        return true;

    }
}
