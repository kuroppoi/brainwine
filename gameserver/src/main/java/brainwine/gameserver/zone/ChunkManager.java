package brainwine.gameserver.zone;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.serialization.BlockDeserializer;
import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.util.ZipUtils;

public class ChunkManager {

    private static final Logger logger = LogManager.getLogger();
    private static final int allocSize = 2048;
    private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .registerModule(new SimpleModule()
                    .addDeserializer(Block.class, BlockDeserializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE));
    private final Zone zone;
    private final File blocksFile;
    private RandomAccessFile file;
    private int dataOffset;
    
    public ChunkManager(Zone zone) {
        this.zone = zone;
        blocksFile = new File(zone.getDirectory(), "blocks.dat");
        File legacyBlocksFile = new File(zone.getDirectory(), "blocks");
        
        if(!blocksFile.exists() && legacyBlocksFile.exists()) {
            logger.info("Updating blocks file for zone {} ...", zone.getDocumentId());
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;
            
            try {
                inputStream = new DataInputStream(new FileInputStream(legacyBlocksFile));
                outputStream = new DataOutputStream(new FileOutputStream(blocksFile));
                int chunkCount = zone.getChunkCount();
                
                for(int i = 0; i < chunkCount; i++) {
                    short length = inputStream.readShort();
                    byte[] chunkBytes = new byte[length];
                    inputStream.read(chunkBytes);
                    inputStream.skipBytes(2048 - length - 2);
                    chunkBytes = ZipUtils.inflateBytes(chunkBytes); 
                    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(chunkBytes);
                    unpacker.unpackArrayHeader();
                    int x = unpacker.unpackInt();
                    int y = unpacker.unpackInt();
                    int width = unpacker.unpackInt();
                    int height = unpacker.unpackInt();
                    Block[] blocks = new Block[unpacker.unpackArrayHeader() / 3];
                    
                    for(int j = 0; j < blocks.length; j++) {
                        blocks[j] = new Block(unpacker.unpackInt(), unpacker.unpackInt(), unpacker.unpackInt());
                    }
                    
                    unpacker.close();
                    byte[] bytes = ZipUtils.deflateBytes(mapper.writeValueAsBytes(new Chunk(x, y, width, height, blocks)));
                    outputStream.writeShort(bytes.length);
                    outputStream.write(bytes);
                    outputStream.write(new byte[allocSize - bytes.length - 2]);
                }
                
                inputStream.close();
                outputStream.close();
            } catch(Exception e) {
                logger.error("Could not update blocks file for zone {}", zone.getDocumentId(), e);
            }
            
            legacyBlocksFile.delete();
        }
    }
    
    public Chunk loadChunk(int index) {
        try {
            if(file == null) {
                file = new RandomAccessFile(blocksFile, "rw");
            }
            
            file.seek(dataOffset + index * allocSize);
            byte[] bytes = new byte[file.readShort()];
            file.read(bytes);
            return mapper.readValue(ZipUtils.inflateBytes(bytes), Chunk.class);
        } catch(Exception e) {
            logger.error("Could not load chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
        
        return null;
    }
    
    public void saveModifiedChunks() {
        for(Chunk chunk : zone.getChunks()) {
            if(chunk.isModified()) {
                saveChunk(chunk);
            }
        }
    }
    
    public void saveChunk(Chunk chunk) {
        int index = zone.getChunkIndex(chunk.getX(), chunk.getY());
        
        try {
            if(file == null) {
                file = new RandomAccessFile(blocksFile, "rw");
            }
            
            byte[] bytes = ZipUtils.deflateBytes(mapper.writeValueAsBytes(chunk));
            
            if(bytes.length > allocSize) {
                throw new IOException("WARNING: bigger than alloc size: " + bytes.length);
            }
            
            file.seek(dataOffset + index * allocSize);
            file.writeShort(bytes.length);
            file.write(bytes);
            chunk.setModified(false);
        } catch (IOException e) {
            logger.error("Could not save chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
    }
}
