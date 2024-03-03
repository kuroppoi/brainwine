package brainwine.gameserver.server.requests;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.BlockMetaMessage;
import brainwine.gameserver.server.messages.BlocksMessage;
import brainwine.gameserver.server.messages.LightMessage;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@RequestInfo(id = 16)
public class BlocksRequest extends PlayerRequest {

    public int[] chunkIndexes;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        
        // TODO threshold should probably be based on chunk size & perception level
        if(!player.isGodMode() && player.getActiveChunkCount() > 64) {
            return;
        }
        
        List<Chunk> chunks = new ArrayList<>();
        List<MetaBlock> metaBlocks = new ArrayList<>();
        int minX = -1;
        int maxX = -1;
        
        for(int index : chunkIndexes) {
            if(!zone.isChunkIndexInBounds(index)) {
                continue;
            }
            
            int x = index % zone.getNumChunksWidth() * zone.getChunkWidth() + zone.getChunkWidth() / 2;
            int y = index / zone.getNumChunksWidth() * zone.getChunkHeight() + zone.getChunkHeight() / 2;
            double distance = Math.hypot(player.getX() - x, player.getY() - y);
            distance = Math.min(distance, Math.hypot(player.getTeleportX() - x, player.getTeleportY() - y));
            
            if(!player.isGodMode() && distance > zone.getChunkWidth() * 5) {
                continue;
            }
            
            Chunk chunk = zone.getChunk(index);
            
            // Kick player if chunk is null (load failure)
            if(chunk == null) {
                player.kick("Chunk load failure.");
                return;
            }
            
            chunks.add(chunk);
            metaBlocks.addAll(zone.getLocalMetaBlocksInChunk(index));
            player.addActiveChunk(index);
            
            if(chunk.getX() < minX || minX == -1) {
                minX = chunk.getX();
            }
            
            if(chunk.getX() + chunk.getWidth() > maxX || maxX == -1) {
                maxX = chunk.getX() + chunk.getWidth();
            }
        }
        
        player.sendMessage(new BlocksMessage(chunks));
        
        for(MetaBlock metaBlock : metaBlocks) {
            player.sendMessage(new BlockMetaMessage(metaBlock));
        }
        
        if(minX >= 0 && maxX >= 0) {
            player.sendMessage(new LightMessage(minX, zone.getSunlight(minX, maxX - minX)));
        }
    }
}
