package brainwine.gameserver.zone;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.serialization.BlockDeserializer;
import brainwine.gameserver.serialization.BlockSerializer;
import brainwine.gameserver.util.ZipUtils;

public class ChunkManager {

    public static final int FILE_SIGNATURE = 0x44574344;
    public static final int FILE_HEADER_SIZE = 64;
    public static final int LATEST_FILE_VERSION = 0x00000001;
    public static final int DEFAULT_CHUNK_ALLOC_SIZE = 2048;
    public static final int CHUNK_HEADER_SIZE = 32;
    public static final byte[] FILE_HEADER_PADDING = new byte[FILE_HEADER_SIZE - 12];
    public static final byte[] CHUNK_HEADER_PADDING = new byte[CHUNK_HEADER_SIZE - 12];
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory())
            .registerModule(new SimpleModule()
                    .addDeserializer(Block.class, BlockDeserializer.INSTANCE)
                    .addSerializer(BlockSerializer.INSTANCE));
    private final Map<Integer, Chunk> chunks = new HashMap<>();
    private final Zone zone;
    private RandomAccessFile file;
    private int allocSize;
    
    public ChunkManager(Zone zone) {
        this.zone = zone;
    }
    
    private void initialize() throws IOException, DataFormatException {
        // Do nothing if already initialized
        if(file != null) {
            return;
        }
        
        File dataDirectory = zone.getDirectory();
        File chunksFileV1 = new File(dataDirectory, "blocks"); // Legacy version (no longer supported)
        File chunksFileV2 = new File(dataDirectory, "blocks.dat"); // Previous version
        File chunksFile = new File(dataDirectory, "chunks.bin"); // Latest version
        
        // Check outdated legacy format
        if(!chunksFileV2.exists() && chunksFileV1.exists()) {
            throw new IOException("Chunk data is outdated. Please try to load this zone with an older server version to update it.");
        }
        
        // Load or initialize header data
        if(chunksFile.exists()) {
            try(DataInputStream inputStream = new DataInputStream(new FileInputStream(chunksFile))) {
                // Check file signature
                if(inputStream.readInt() != FILE_SIGNATURE) {
                    throw new IOException("Invalid file signature");
                }
                
                int fileVersion = inputStream.readInt();
                allocSize = inputStream.readInt();
                inputStream.skip(FILE_HEADER_PADDING.length);
                
                // Update chunk data if necessary
                if(fileVersion != LATEST_FILE_VERSION) {
                    throw new IOException("Invalid file version"); // Throw exception for now since there is only one version
                }
            }
        } else {
            allocSize = DEFAULT_CHUNK_ALLOC_SIZE;
            
            try(DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(chunksFile))) {
                outputStream.writeInt(FILE_SIGNATURE);
                outputStream.writeInt(LATEST_FILE_VERSION);
                outputStream.writeInt(allocSize);
                outputStream.write(FILE_HEADER_PADDING);
                
                // Update chunk data from previous version if it is present
                if(chunksFileV2.exists()) {
                    logger.info(SERVER_MARKER, "Updating chunk data for zone {} ...", zone.getDocumentId());
                    int chunkCount = zone.getChunkCount();
                    long now = System.currentTimeMillis();
                    
                    try(DataInputStream inputStream = new DataInputStream(new FileInputStream(chunksFileV2))) {                        
                        for(int i = 0; i < chunkCount; i++) {
                            // Read chunk data
                            byte[] chunkBytes = new byte[inputStream.readShort()];
                            inputStream.read(chunkBytes);
                            inputStream.skip(DEFAULT_CHUNK_ALLOC_SIZE - chunkBytes.length - 2); // Skip reserved chunk space
                            
                            // Write chunk header
                            outputStream.writeLong(now); // Save time
                            outputStream.writeInt(chunkBytes.length);
                            outputStream.write(CHUNK_HEADER_PADDING);
                            
                            // Write chunk data
                            outputStream.write(chunkBytes);
                            
                            // Write chunk padding
                            if(i + 1 < chunkCount) {
                                outputStream.write(new byte[allocSize - chunkBytes.length - CHUNK_HEADER_SIZE]);
                            }
                        }
                    }
                }
            }
        }
        
        // Create random access file stream
        file = new RandomAccessFile(chunksFile, "rw");
    }
    
    protected void closeStream() {
        if(file != null) {
            try {
                file.close();
            } catch(IOException e) {
                logger.error(SERVER_MARKER, "Could not close blocks file stream for zone {}", zone.getDocumentId());
            } finally {
                file = null;
            }
        }
    }
    
    public void saveChunks() {
        List<Chunk> inactiveChunks = new ArrayList<>();
        
        for(Chunk chunk : chunks.values()) {
            saveChunk(chunk);
            boolean active = false;
            
            for(Player player : zone.getPlayers()) {
                if(player.isChunkActive(chunk)) {
                    active = true;
                    break;
                }
            }
            
            if(!active) {
                inactiveChunks.add(chunk);
            }
        }
        
        for(Chunk chunk : inactiveChunks) {
            chunks.remove(getChunkIndex(chunk.getX(), chunk.getY()));
            zone.onChunkUnloaded(chunk);
        }
    }
    
    private void saveChunk(Chunk chunk) {
        int index = zone.getChunkIndex(chunk.getX(), chunk.getY());
        
        try {
            initialize();
            file.seek(FILE_HEADER_SIZE + index * allocSize);
            file.writeLong(System.currentTimeMillis()); // Write save time
            
            // Write block data if chunk has been modified
            if(chunk.isModified()) {
                byte[] bytes = ZipUtils.deflateBytes(mapper.writeValueAsBytes(chunk));
                
                // TODO reformat entire file with bigger alloc size
                if(bytes.length > allocSize - CHUNK_HEADER_SIZE) {
                    throw new IOException("WARNING: bigger than alloc size: " + bytes.length);
                }
                
                file.writeInt(bytes.length);
                file.write(CHUNK_HEADER_PADDING);
                file.write(bytes);
                chunk.setModified(false);
            }
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not save chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
    }
    
    private Chunk loadChunk(int index) {
        try {
            initialize();
            file.seek(FILE_HEADER_SIZE + index * allocSize);
            long saveTime = file.readLong();
            byte[] bytes = new byte[file.readInt()];
            file.skipBytes(CHUNK_HEADER_PADDING.length);
            file.read(bytes);
            Chunk chunk = mapper.readValue(ZipUtils.inflateBytes(bytes), Chunk.class);
            chunk.setSaveTime(saveTime);
            return chunk;
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Could not load chunk {} of zone {}", index, zone.getDocumentId(), e);
        }
        
        return null;
    }
    
    public List<Chunk> getVisibleChunks() {
        List<Chunk> visibleChunks = new ArrayList<>();
        Set<Integer> chunkIndices = new HashSet<>();
        int chunkWidth = zone.getChunkWidth();
        int chunkHeight = zone.getChunkHeight();
        
        for(Player player : zone.getPlayers()) {
            int x = (int)player.getX();
            int y = (int)player.getY();
            
            // TODO take screen size & perception skill into account
            for(int i = -40; i <= 40; i += chunkWidth) {
                for(int j = -20; j <= 20; j += chunkHeight) {
                    chunkIndices.add(getChunkIndex(x + i, y + j));
                }
            }
        }
        
        for(int chunkIndex : chunkIndices) {
            if(isChunkLoaded(chunkIndex)) {
                visibleChunks.add(chunks.get(chunkIndex));
            }
        }
        
        return visibleChunks;
    }
    
    public void putChunk(int index, Chunk chunk) {
        if(!chunks.containsKey(index) && isChunkIndexInBounds(index)) {
            chunk.setModified(true);
            chunks.put(index, chunk);
        }
    }
    
    public boolean isChunkLoaded(int x, int y) {
        return zone.areCoordinatesInBounds(x, y) && isChunkLoaded(getChunkIndex(x, y));
    }
    
    public boolean isChunkLoaded(int index) {
        return chunks.containsKey(index);
    }
    
    public boolean isChunkIndexInBounds(int index) {
        return index >= 0 && index < zone.getNumChunksWidth() * zone.getNumChunksHeight();
    }
    
    public int getChunkIndex(int x, int y) {
        return y / zone.getChunkHeight() * zone.getNumChunksWidth() + x / zone.getChunkWidth();
    }
    
    public Chunk getChunk(int x, int y) {
        return getChunk(getChunkIndex(x, y));
    }
    
    public Chunk getChunk(int index) {
        if(!isChunkIndexInBounds(index)) {
            return null;
        }
        
        Chunk chunk = chunks.get(index);
        
        // Load chunk if it isn't cached
        if(chunk == null) {
            chunk = loadChunk(index);
            
            // Index chunk if it was loaded successfully
            if(chunk != null) {
                chunks.put(index, chunk);
                zone.onChunkLoaded(chunk);
            }
        }
        
        return chunk;
    }
    
    public Collection<Chunk> getLoadedChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }
}
