package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.player.ChatType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.MetaType;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.BlockMetaMessage;
import brainwine.gameserver.server.messages.ChatMessage;
import brainwine.gameserver.server.messages.ConfigurationMessage;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.server.messages.LightMessage;
import brainwine.gameserver.server.messages.ZoneExploredMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.util.MathUtils;

@JsonIncludeProperties({"name", "biome", "width", "height"})
public class Zone {
    
    public static final int DEFAULT_CHUNK_WIDTH = 20;
    public static final int DEFAULT_CHUNK_HEIGHT = 20;
    private final String documentId;
    private String name;
    private final Biome biome;
    private final int width;
    private final int height;
    private final int chunkWidth;
    private final int chunkHeight;
    private final int numChunksWidth;
    private final int numChunksHeight;
    private final int[] surface;
    private final int[] sunlight;
    private final boolean[] chunksExplored;
    private float time = 5000;
    private float temperature = 0;
    private float wind = 0;
    private float cloudiness = 5000;
    private float precipitation = 0;
    private float acidity = 0;
    private final ChunkIOManager chunkManager;
    private final Set<Integer> pendingSunlight = new HashSet<>();
    private final Map<Integer, Entity> entities = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final Map<Integer, Chunk> chunks = new HashMap<>();
    private final Map<Integer, MetaBlock> metaBlocks = new HashMap<>();
    private final Map<Integer, MetaBlock> globalMetaBlocks = new HashMap<>();
    private final Map<Integer, MetaBlock> fieldBlocks = new HashMap<>();
    
    public Zone(String documentId, String name, Biome biome, SizePreset sizePreset) {
        this(documentId, name, biome, sizePreset.getWidth(), sizePreset.getHeight());
    }
    
    @ConstructorProperties({"documentId", "name", "biome", "width", "height"})
    public Zone(@JacksonInject("documentId") String documentId, String name, Biome biome, int width, int height) {
        this(documentId, name, biome, width, height, DEFAULT_CHUNK_WIDTH, DEFAULT_CHUNK_HEIGHT);
    }
    
    private Zone(String documentId, String name, Biome biome, int width, int height, int chunkWidth, int chunkHeight) {
        this.documentId = documentId;
        this.name = name;
        this.biome = biome;
        this.width = width;
        this.height = height;
        this.chunkWidth = chunkWidth;
        this.chunkHeight = chunkHeight;
        chunkManager = new ChunkIOManager(this);
        numChunksWidth = width / chunkWidth;
        numChunksHeight = height / chunkHeight;
        surface = new int[width];
        sunlight = new int[width];
        chunksExplored = new boolean[numChunksWidth * numChunksHeight];
        Arrays.fill(surface, height);
        Arrays.fill(sunlight, height);
    }
    
    @JsonCreator
    private static Zone fromId(String id) {
        return GameServer.getInstance().getZoneManager().getZone(id);
    }
    
    public void tick() {
        for(Entity entity : getEntities()) {
            entity.tick();
        }
    }
    
    public void saveModifiedChunks() {
        chunkManager.saveModifiedChunks();
        removeInactiveChunks();
    }
    
    /**
     * Sends a message to each player in this zone.
     * 
     * @param message The message to send.
     */
    public void sendMessage(Message message) {
        for(Player player : players) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to each player who is near the specified chunk.
     * 
     * @param message The message to send.
     * @param chunk The chunk near which players must be.
     */
    public void sendMessageToChunk(Message message, Chunk chunk) {
        for(Player player : players) {
            if(player.isChunkActive(chunk)) {
                player.sendMessage(message);
            }
        }
    }
    
    public void chat(Player sender, String text) {
        chat(sender, text, ChatType.CHAT);
    }
    
    /**
     * Broadcasts a chat message to all players.
     * 
     * @param sender The player who sent the message.
     * @param text The text.
     * @param type The display type.
     */
    public void chat(Player sender, String text, ChatType type) {
        sendMessage(new ChatMessage(sender.getId(), text, type));
    }
    
    public boolean isBlockOccupied(int x, int y, Layer layer) {
        if(!areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        Block block = getBlock(x, y);
        Item item = block.getItem(layer);
        
        return !item.isAir() && !item.canPlaceOver();
    }
    
    public boolean isBlockProtected(int x, int y, Player from) {
        for(MetaBlock fieldBlock : fieldBlocks.values()) {
            Item item = fieldBlock.getItem();
            int fX = fieldBlock.getX();
            int fY = fieldBlock.getY();
            int field = fieldBlock.getItem().getField();
            
            if(item.isDish() && !ownsMetaBlock(fieldBlock, from)) {
                if(MathUtils.inRange(x, y, fX, fY, field)) {
                    return true;
                }
            } else if(item.hasField() && !ownsMetaBlock(fieldBlock, from)) {
                if(x == fX && y == fY) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean willDishOverlap(int x, int y, int field, Player player) {
        for(MetaBlock fieldBlock : fieldBlocks.values()) {
            int fX = fieldBlock.getX();
            int fY = fieldBlock.getY();
            int fField = fieldBlock.getItem().getField();
            
            if(MathUtils.inRange(x, y, fX, fY, field + fField) && !ownsMetaBlock(fieldBlock, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod) {
        updateBlock(x, y, layer, item, mod, null);
    }
    
    public void updateBlock(int x, int y, Layer layer, int item, int mod, Player owner) {
        updateBlock(x, y, layer, ItemRegistry.getItem(item), mod, owner);
    }
    
    public void updateBlock(int x, int y, Layer layer, Item item, int mod) {
        updateBlock(x, y, layer, item, mod, null);
    }
    
    /**
     * Updates the block at the specified position, if the coordinates are in bounds.
     * Also creates/deletes any metadata & updates sunlight if applicable.
     * 
     * @param x The x position of the block.
     * @param y The y position of the block.
     * @param layer The layer to modify.
     * @param item The item to set it to.
     * @param mod The mod to set it to.
     * @param owner The owner of the block, can be null.
     */
    public void updateBlock(int x, int y, Layer layer, Item item, int mod, Player owner) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        Chunk chunk = getChunk(x, y);
        chunk.getBlock(x, y).updateLayer(layer, item, mod);
        chunk.setModified(true);
        sendMessageToChunk(new BlockChangeMessage(x, y, layer, item, mod), chunk);
        
        if(layer == Layer.FRONT) {
            setMetaBlock(x, y, item, owner);
            
            if(item.isWhole() && y < sunlight[x]) {
                // If we place the block higher than the current sunlight, move the sunlight there.
                sunlight[x] = y;
            } else if(!item.isWhole() && y == sunlight[x]) {
                // If we break the block where the sunlight is at, recalculate from that point.
                recalculateSunlight(x, sunlight[x]);
            }
            
            sendMessageToChunk(new LightMessage(x, getSunlight(x, 1)), chunk);
        }
    }
    
    /**
     * @param x The x position of the block.
     * @param y The y position of the block.
     * @return The index of the block at the specified coordinates.
     */
    public int getBlockIndex(int x, int y) {
        return y * width + x;
    }
    
    public Block getBlock(int x, int y) {
        if(areCoordinatesInBounds(x, y)) {
            Chunk chunk = getChunk(x, y);
            return chunk.getBlock(x, y);
        }
        
        return null;
    }
    
    public void setMetaBlock(int x, int y, int item) {
        setMetaBlock(x, y, item, null);
    }
    
    public void setMetaBlock(int x, int y, int item, Player owner) {
        setMetaBlock(x, y, item, owner, new HashMap<String, Object>());
    }
    
    public void setMetaBlock(int x, int y, int item, Player owner, Map<String, Object> metadata) {
        setMetaBlock(x, y, ItemRegistry.getItem(item), owner, metadata);
    }
    
    public void setMetaBlock(int x, int y, Item item) {
        setMetaBlock(x, y, item, null);
    }
    
    public void setMetaBlock(int x, int y, Item item, Player owner) {
        setMetaBlock(x, y, item, owner, new HashMap<String, Object>());
    }
    
    /**
     * Sets the metadata of the block at the specified position.
     * If the provided item has no meta type, existing metadata will be removed instead.
     */
    public void setMetaBlock(int x, int y, Item item, Player owner, Map<String, Object> metadata) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        Chunk chunk = getChunk(x, y);
        int blockIndex = getBlockIndex(x, y);
        MetaBlock metaBlock = null;
        
        if(item.hasMeta()) {
            metaBlock = new MetaBlock(x, y, item, metadata);
            
            if(owner != null) {
                metaBlock.setOwner(owner.getDocumentId());
            }
            
            metaBlocks.put(blockIndex, metaBlock);
            
            if(item.hasField()) {
                fieldBlocks.put(blockIndex, metaBlock);
            }
            
            MetaType meta = item.getMeta();
            
            if(meta == MetaType.GLOBAL) {
                globalMetaBlocks.put(blockIndex, metaBlock);
                sendMessage(new BlockMetaMessage(x, y)); // Landmarks are not properly updated on the client unless they are removed first.
                sendMessage(new BlockMetaMessage(metaBlock));
            } else if(meta == MetaType.LOCAL) {
                sendMessageToChunk(new BlockMetaMessage(metaBlock), chunk);
            }
        } else if((metaBlock = metaBlocks.remove(blockIndex)) != null) {
            globalMetaBlocks.remove(blockIndex);
            fieldBlocks.remove(blockIndex);
            MetaType meta = metaBlock.getItem().getMeta();
            
            if(meta == MetaType.GLOBAL) {
                sendMessage(new BlockMetaMessage(x, y));
            } else if(meta == MetaType.LOCAL) {
                sendMessageToChunk(new BlockMetaMessage(x, y), chunk);
            }
        }
    }
    
    /**
     * TODO bad
     */
    public void setMetaBlock(int x, int y, MetaBlock metaBlock) {
        if(!areCoordinatesInBounds(x, y)) {
            return;
        }
        
        int blockIndex = getBlockIndex(x, y);
        
        if(metaBlock != null) {
            metaBlocks.put(blockIndex, metaBlock);
            
            if(metaBlock.getItem().getMeta() == MetaType.GLOBAL) {
                globalMetaBlocks.put(blockIndex, metaBlock);
            }
            
            if(metaBlock.getItem().hasField()) {
                fieldBlocks.put(blockIndex, metaBlock);
            }
        } else if((metaBlock = metaBlocks.remove(blockIndex)) != null) {
            globalMetaBlocks.remove(blockIndex);
            fieldBlocks.remove(blockIndex);
        }
    }
    
    private boolean ownsMetaBlock(MetaBlock metaBlock, Player player) {
        if(!metaBlock.hasOwner()) {
            return false;
        }
        
        return player.getDocumentId().equals(metaBlock.getOwner());
    }
    
    public List<MetaBlock> getLocalMetaBlocksInChunk(int chunkIndex) {
        List<MetaBlock> metaBlocks = new ArrayList<>();
        
        for(MetaBlock metaBlock : getMetaBlocks()) {
            if(chunkIndex == getChunkIndex(metaBlock.getX(), metaBlock.getY())) {
                if(metaBlock.getItem().getMeta() == MetaType.LOCAL) {
                    metaBlocks.add(metaBlock);
                }
            }
        }
        
        return metaBlocks;
    }
    
    public MetaBlock getRandomZoneTeleporter() {
        List<MetaBlock> zoneTeleporters = new ArrayList<>();
        
        for(MetaBlock fieldBlock : fieldBlocks.values()) {
            if(fieldBlock.getItem().hasUse(ItemUseType.ZONE_TELEPORT)) {
                zoneTeleporters.add(fieldBlock);
            }
        }
        
        if(zoneTeleporters.isEmpty()) {
            return null;
        }
        
        return zoneTeleporters.get((int)(Math.ceil(Math.random()) * (zoneTeleporters.size() - 1)));
    }
    
    public Collection<MetaBlock> getMetaBlocks() {
        return metaBlocks.values();
    }
    
    public Collection<MetaBlock> getGlobalMetaBlocks() {
        return globalMetaBlocks.values();
    }
    
    public void addPlayer(Player player) {
        addEntity(player);
        player.onZoneChanged();
        player.sendMessageToPeers(new EntityStatusMessage(player, EntityStatus.ENTERING));
        player.sendMessageToPeers(new EntityPositionMessage(player));
        players.add(player);
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
        player.sendMessageToPeers(new EntityStatusMessage(player, EntityStatus.EXITING));
        removeEntity(player);
    }
    
    private void addEntity(Entity entity) {
        entity.setZone(this);
        entities.put(entity.getId(), entity);
    }
    
    private void removeEntity(Entity entity) {
        entities.remove(entity.getId());
    }
    
    public Collection<Entity> getEntities() {
        return entities.values();
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public boolean areCoordinatesInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
    
    private void removeInactiveChunks() {
        Iterator<Entry<Integer, Chunk>> iterator = chunks.entrySet().iterator();
        
        while(iterator.hasNext()) {
            Entry<Integer, Chunk> entry = iterator.next();
            Chunk chunk = entry.getValue();
            boolean active = false;
            
            for(Player player : players) {
                if(player.isChunkActive(chunk)) {
                    active = true;
                    break;
                }
            }
            
            if(!active) {
                iterator.remove();
            }
        }
    }
    
    public boolean isChunkIndexInBounds(int index) {
        return index >= 0 && index < numChunksWidth * numChunksHeight;
    }
    
    public boolean isChunkLoaded(int x, int y) {
        return isChunkLoaded(getChunkIndex(x, y));
    }
    
    public boolean isChunkLoaded(int index) {
        return chunks.containsKey(index);
    }
    
    public void putChunk(int index, Chunk chunk) {
        if(!chunks.containsKey(index) && isChunkIndexInBounds(index)) {
            chunk.setModified(true);
            chunks.put(index, chunk);
        }
    }
    
    public int getChunkIndex(int x, int y) {
        return y / chunkHeight * numChunksWidth + x / chunkWidth;
    }
    
    public Chunk getChunk(int x, int y) {
        return getChunk(getChunkIndex(x, y));
    }
    
    public Chunk getChunk(int index) {
        if(!isChunkIndexInBounds(index)) {
            return null;
        }
        
        Chunk chunk = chunks.get(index);
        
        if(chunk == null) {
            chunk = chunkManager.loadChunk(index);
            tryRecalculatePendingSunlight(chunk);
            chunks.put(index, chunk);
        }
        
        return chunk;
    }
    
    public Collection<Chunk> getChunks() {
        return chunks.values();
    }
    
    public void setPendingSunlight(int[] sunlight) {
        pendingSunlight.clear();
        
        for(int i : sunlight) {
            pendingSunlight.add(i);
        }
    }
    
    private void tryRecalculatePendingSunlight(Chunk chunk) {
        if(!pendingSunlight.isEmpty()) {
            int chunkX = chunk.getX();
            
            for(int x = chunkX; x < chunkX + chunk.getWidth(); x++) {
                if(pendingSunlight.contains(x)) {
                    recalculateSunlight(x, sunlight[x]);
                    sendMessageToChunk(new LightMessage(x, getSunlight(x, 1)), chunk);
                }
            }
        }
    }
    
    public Set<Integer> getPendingSunlight() {
        return pendingSunlight;
    }
    
    public void setSurface(int x, int surface) {
        if(x < 0 || x >= width) {
            return;
        }
        
        this.surface[x] = surface;
    }
    
    public void setSurface(int[] surface) {
        if(surface.length != width) {
            return;
        }
        
        System.arraycopy(surface, 0, this.surface, 0, width);
    }
    
    public int[] getSurface() {
        return surface;
    }
    
    public void recalculateSunlight(int x, int startY) {
        for(int y = startY; y < height; y++) {
            if(isChunkLoaded(x, y)) {
                Block block = getBlock(x, y);
                
                if(block.getFrontItem().isWhole()) {
                    sunlight[x] = y;
                    return;
                }
            } else {
                sunlight[x] = y;
                pendingSunlight.add(x);
                return;
            }
        }
    }
    
    public void setSunlight(int x, int sunlight) {
        if(x < 0 || x >= width) {
            return;
        }
        
        this.sunlight[x] = sunlight;
    }
    
    public void setSunlight(int[] sunlight) {
        if(sunlight.length != width) {
            return;
        }
        
        System.arraycopy(sunlight, 0, this.sunlight, 0, width);
    }
    
    public int[] getSunlight(int x, int length) {
        int[] sunlight = new int[length];
        
        if(x >= 0 && x + length < width) {
            System.arraycopy(this.sunlight, x, sunlight, 0, length);
        }
        
        return sunlight;
    }
    
    public int[] getSunlight() {
        return sunlight;
    }
    
    public boolean exploreArea(int x, int y) {
        if(!areCoordinatesInBounds(x, y)) {
            return false;
        }
        
        int chunkIndex = getChunkIndex(x, y);
        
        if(chunksExplored[chunkIndex]) {
            return false;
        }
        
        sendMessage(new ZoneExploredMessage(chunkIndex));
        return chunksExplored[chunkIndex] = true;
    }
    
    public void setChunksExplored(boolean[] chunksExplored) {
        if(chunksExplored.length != getChunkCount()) {
            return;
        }
        
        System.arraycopy(chunksExplored, 0, this.chunksExplored, 0, chunksExplored.length);
    }
    
    /**
     * @return A float between 0 and 1, where 0 is completely unexplored and 1 is fully explored.
     */
    public float getExplorationProgress() {
        int numChunksExplored = 0;
        
        for(boolean flag : chunksExplored) {
            if(flag) {
                numChunksExplored++;
            }
        }
        
        return (float)numChunksExplored / (numChunksWidth * numChunksHeight);
    }
    
    public boolean[] getChunksExplored() {
        return chunksExplored;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public int getSeed() {
        return (int)(UUID.fromString(documentId).getMostSignificantBits() >> 32);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public Biome getBiome() {
        return biome;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getChunkWidth() {
        return chunkWidth;
    }
    
    public int getChunkHeight() {
        return chunkHeight;
    }
    
    public int getNumChunksWidth() {
        return numChunksWidth;
    }
    
    public int getNumChunksHeight() {
        return numChunksHeight;
    }
    
    public int getChunkCount() {
        return numChunksWidth * numChunksHeight;
    }
    
    @JsonValue
    protected Map<String, Object> getJsonConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("name", name);
        config.put("biome", biome);
        config.put("width", width);
        config.put("height", height);
        return config;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ConfigurationMessage}.
     */
    public Map<String, Object> getClientConfig(Player player) {
        Map<String, Object> config = new HashMap<>();
        config.put("id", documentId);
        config.put("name", name);
        config.put("biome", biome);
        config.put("size", new int[]{width, height});
        config.put("chunk_size", new int[]{chunkWidth, chunkHeight});
        config.put("surface", surface);
        config.put("chunks_explored", chunksExplored);
        Map<String, Object> depth = new HashMap<>();
        List<Object> earth = new ArrayList<>();
        
        if(player.isV3()) {
            earth.add(Arrays.asList(height * 0.9, "ground/earth-deepest"));
            earth.add(Arrays.asList(height * 0.7, "ground/earth-deeper"));
            earth.add(Arrays.asList(height * 0.45, "ground/earth-deep"));
            depth.put("ground/earth", earth);
        } else {
            String key = biome == Biome.PLAIN ? "temperate" : biome.getId();
            earth.add(Arrays.asList(height * 0.45, String.format("%s/earth-front-deep", key)));
            earth.add(Arrays.asList(height * 0.7, String.format("%s/earth-front-deeper", key)));
            earth.add(Arrays.asList(height * 0.9, String.format("%s/earth-front-deepest", key)));
            depth.put(String.format("%s/earth-front", key), earth);
        }
        
        config.put("depth", depth);
        return config;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ZoneStatusMessage}.
     */
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("w", new float[]{time, temperature, wind, cloudiness, precipitation, acidity});
        return config;
    }
    
    /**
     * @return A {@link Map} containing all the portal-related data.
     */
    public Map<String, Object> getPortalConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("name", name);
        config.put("biome", biome);
        config.put("players", players.size());
        config.put("explored", getExplorationProgress());
        config.put("gen_date", "2021-02-15"); // format = yyyy-mm-dd
        return config;
    }
}
