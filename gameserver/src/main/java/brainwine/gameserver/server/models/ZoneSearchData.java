package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

@JsonFormat(shape = Shape.ARRAY)
public class ZoneSearchData {
    
    public final String id;
    public final String name;
    public final int playerCount;
    public final int followeeCount;
    public final String[] followees;
    public final int activeDuration;
    public final int explorationProgress;
    public final Biome biome;
    public final String status;
    public final String accessibility; // 'a' = all, 'p' = premium
    public final int protectionLevel;
    public final String scenario; // Market, PvP, etc.
    
    public ZoneSearchData(Zone zone, Player player) {
        this.id = zone.getDocumentId();
        this.name = zone.getName();
        this.playerCount = zone.getPlayerCount();
        this.followeeCount = 0; // TODO
        this.followees = new String[0]; // TODO
        this.activeDuration = 0; // TODO
        this.explorationProgress = (int)(zone.getExplorationProgress() * 100);
        this.biome = zone.getBiome();
        this.status = zone.getBiome() == Biome.PLAIN ? (zone.isPurified() ? "purified" : "toxic") : null;
        this.accessibility = "a";
        this.protectionLevel = zone.isProtected(player) ? 10 : 0;
        this.scenario = null; // TODO
    }
}
