package brainwine.gameserver.zone;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.util.ZipUtils;

/**
 * TODO in the event of a load/save failure, the zone should shut-down.
 */
public class ChunkIOManager {
    
    public static final int ALLOC_SIZE = 2048;
    private static final Logger logger = LogManager.getLogger();
    private final Zone zone;
    private RandomAccessFile dataFile;
    
    public ChunkIOManager(Zone zone) {
        this.zone = zone;
        File dataDir = new File("zones", zone.getDocumentId());
        dataDir.mkdirs();
        File blockDataFile = new File(dataDir, "blocks");
        
        try {
            if(!blockDataFile.exists()) {
                blockDataFile.createNewFile();
            }
            
            dataFile = new RandomAccessFile(blockDataFile, "rw");
        } catch(IOException e) {
            logger.error("ChunkIOManager construction for zone {} failed", zone.getDocumentId(), e);
        }
    }
    
    public void saveModifiedChunks() {
        for(Chunk chunk : zone.getChunks()) {
            if(chunk.isModified()) {
                chunk.setModified(false);
                saveChunk(chunk);
            }
        }
    }
    
    public void saveChunk(Chunk chunk) {
        int index = zone.getChunkIndex(chunk.getX(), chunk.getY());
        
        try {
            BufferPacker packer = MessagePackHelper.createBufferPacker();
            packer.write(chunk);
            byte[] bytes = packer.toByteArray();
            packer.close();
            bytes = ZipUtils.deflateBytes(bytes);
            dataFile.seek(index * ALLOC_SIZE);
            dataFile.writeShort(bytes.length);
            dataFile.write(bytes);
        } catch(Exception e) {
            logger.error("Could not save chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
    }
    
    public Chunk loadChunk(int index) {
        Chunk chunk = null;
        
        try {
            dataFile.seek(index * ALLOC_SIZE);
            byte[] bytes = new byte[dataFile.readShort()];
            dataFile.read(bytes);
            BufferUnpacker unpacker = MessagePackHelper.createBufferUnpacker(ZipUtils.inflateBytes(bytes));
            chunk = unpacker.read(Chunk.class);
            unpacker.close();
        } catch(Exception e) {
            logger.error("Could not load chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
        
        return chunk;
    }
}
